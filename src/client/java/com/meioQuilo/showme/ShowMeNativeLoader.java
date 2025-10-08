package com.meioQuilo.showme;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ShowMeNativeLoader {
    public static String getNativeLibName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            return "gpu_monitor.dll";
        if (os.contains("mac"))
            return "libgpu_monitor.dylib";

        return "libgpu_monitor.so";
    }

    public static String extractNativeLib() throws IOException {
        String libName = getNativeLibName();
        String resourcePath = "/natives/" + libName;

        // File tempFile = File.createTempFile("gpu_monitor", libName);
        File tempFile = new File(System.getProperty("java.io.tmpdir"), libName);
        tempFile.deleteOnExit();
        System.out.println("Temp Criado");

        try (InputStream in = ShowMeClient.class.getResourceAsStream(resourcePath);
                OutputStream out = new FileOutputStream(tempFile)) {

            System.out.println("In e Out");
            if (in == null) {
                throw new IOException("Lib not found in: " + resourcePath);
            }

            byte[] buffer = new byte[4096];
            int bytesRead;

            System.out.println("Ler bytes");
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

        }

        return tempFile.getAbsolutePath();
    }

    public static void loadNative() {

        try {
            String libPath = extractNativeLib();
            System.out.println(libPath);
            System.load(libPath);
            System.out.println("JNI Loaded!");
        } catch (Exception e) {
            System.err.println("JNI was not loaded");
            e.printStackTrace();
        }

    }

}
