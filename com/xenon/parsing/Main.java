package com.xenon.parsing;

import java.nio.file.Paths;

public class Main {

    public static void main(String[] __) throws Exception{

        System.out.println(TOMLWorker.parse(Paths.get("./test.toml")));
    }
}
