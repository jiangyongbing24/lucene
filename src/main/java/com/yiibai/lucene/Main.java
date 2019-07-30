package com.yiibai.lucene;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @Created by JYB
 * @Date 2019/7/30 21:27
 * @Description TODO
 */
public class Main {
    public static void main(String[] args) {
        BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
        try{
            while(true){
                System.out.print( "1-添加索引 2-搜索索引:");
                String choose = scanner.readLine();
                if(!"1".equals(choose) && !"2".equals(choose)){
                    System.out.println("不合法的输入");
                    System.out.println();
                    continue;
                }
                if("1".equals(choose)){
                    System.out.print("需要索引的文件夹路径:");
                    String indexPath = scanner.readLine();
                    IndexFiles.createIndex(indexPath);
                }
                else if("2".equals(choose)){
                    SearchFiles.searchFile(scanner);
                }
                System.out.println();
                System.out.print("是否继续?(按任意键退出，y继续):");
                String isExit = scanner.readLine();
                if("y".equals(isExit) || "Y".equals(isExit)) {
                    System.out.println();
                    continue;
                }
                else
                    return;
            }
        }catch (Exception e){}
    }
}
