package com.yiibai.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchFiles {
    private SearchFiles() {
    }

    public static void searchFile(BufferedReader in) throws Exception{
        String index = "index";
        String field = LuceneConstants.CONTENTS;
        String queries = null;
        int repeat = 0;
        boolean raw = false;
        String queryString = null;
        int hitsPerPage = 10;

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        if (queries != null) {
            in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
        } else {
            in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        }

        QueryParser parser = new QueryParser(field, analyzer);

        if (queries == null && queryString == null) {
            System.out.println("输入关键字: ");
        }

        String line = queryString != null ? queryString : in.readLine();
        if ("".equals(line) || line == null || line.length() == -1) {
            return;
        }

        Query query = parser.parse(line);
        System.out.println("查找关键字: " + query.toString(field));
        if (repeat > 0) {
            Date start = new Date();

            for(int i = 0; i < repeat; ++i) {
                searcher.search(query, 100);
            }

            Date end = new Date();
            System.out.println("时间: " + (end.getTime() - start.getTime()) + "毫秒");
        }

        doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);

        reader.close();
    }

    public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, int hitsPerPage, boolean raw, boolean interactive) throws IOException {
        TopDocs results = searcher.search(query, 5 * hitsPerPage);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = Math.toIntExact(results.totalHits.value);
        System.out.println(numTotalHits + " 条匹配文档");
        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);

        while(true) {
            if (end > hits.length) {
                System.out.println("仅收集了 1 - " + hits.length + " 至 " + numTotalHits + " 条匹配的文档集合");
                System.out.println("查看更多 (y/n) ?");
                String line = in.readLine();
                if (line.length() == 0 || line.charAt(0) == 'n') {
                    break;
                }

                hits = searcher.search(query, numTotalHits).scoreDocs;
            }

            end = Math.min(hits.length, start + hitsPerPage);

            for(int i = start; i < end; ++i) {
                if (raw) {
                    System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);
                } else {
                    Document doc = searcher.doc(hits[i].doc);
                    String path = doc.get("path");
                    if (path != null) {
                        System.out.println(i + 1 + ". " + path);
                        String title = doc.get("title");
                        if (title != null) {
                            System.out.println("文档的Title: " + doc.get("title"));
                        }
                    } else {
                        System.out.println(i + 1 + ". 没有这个文件的路径");
                    }
                }
            }

            if (!interactive || end == 0) {
                break;
            }

            if (numTotalHits >= end) {
                boolean quit = false;

                while(true) {
                    System.out.print("按键 ");
                    if (start - hitsPerPage >= 0) {
                        System.out.print("(p)查看上一页, ");
                    }

                    if (start + hitsPerPage < numTotalHits) {
                        System.out.print("(n)下一页, ");
                    }

                    System.out.println("(q)退出分页或者输入数字跳转到对应的页码");
                    String line = in.readLine();
                    if (line.length() == 0 || line.charAt(0) == 'q') {
                        quit = true;
                        break;
                    }

                    if (line.charAt(0) == 'p') {
                        start = Math.max(0, start - hitsPerPage);
                        break;
                    }

                    if (line.charAt(0) == 'n') {
                        if (start + hitsPerPage < numTotalHits) {
                            start += hitsPerPage;
                        }
                        break;
                    }

                    if(!line.trim().matches("^[0-9]*$")) {
                        System.out.println("无法识别的输入");
                        return;
                    }
                    int page = Integer.parseInt(line);
                    if ((page - 1) * hitsPerPage < numTotalHits) {
                        start = (page - 1) * hitsPerPage;
                        break;
                    }

                    System.out.println("不存在的页码");
                }

                if (quit) {
                    break;
                }

                end = Math.min(numTotalHits, start + hitsPerPage);
            }
        }

    }
}
