package org.example.statistics.util;

import org.example.common.exception.BusinessException;
import org.example.statistics.entity.ClosingReport;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 结案报告批量 Zip 打包下载
 */
public final class ZipDownloadUtil {

    private ZipDownloadUtil() {}

    public static void downloadReportsZip(OutputStream outputStream,
                                         List<ClosingReport> reports,
                                         String filesDir) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            int index = 0;
            for (ClosingReport report : reports) {
                index++;
                String baseName = String.format("%s_%s_咨询师%d_%d",
                        safeName(report.getStudentNo()),
                        safeName(report.getStudentName()),
                        report.getCounselorId() != null ? report.getCounselorId() : 0,
                        report.getId());
                String filePath = report.getFilePath();
                File file = resolveFile(filePath, filesDir);
                if (file != null && file.exists() && file.isFile()) {
                    addFileEntry(zos, file, baseName + getExtension(file.getName()));
                } else {
                    String content = "结案报告 ID=" + report.getId()
                            + "，Word 文件尚未生成。请先在咨询师端提交报告并生成 Word。";
                    addTextEntry(zos, baseName + ".txt", content);
                }
            }
            if (index == 0) {
                throw new BusinessException("没有可下载的报告");
            }
        }
    }

    private static File resolveFile(String filePath, String filesDir) {
        if (filePath == null || filePath.isBlank()) {
            return null;
        }
        File direct = new File(filePath);
        if (direct.exists()) {
            return direct;
        }
        if (filesDir != null && !filesDir.isBlank()) {
            File underDir = new File(filesDir, new File(filePath).getName());
            if (underDir.exists()) {
                return underDir;
            }
        }
        return null;
    }

    private static void addFileEntry(ZipOutputStream zos, File file, String entryName) throws IOException {
        zos.putNextEntry(new ZipEntry(entryName));
        Files.copy(file.toPath(), zos);
        zos.closeEntry();
    }

    private static void addTextEntry(ZipOutputStream zos, String entryName, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(entryName));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String safeName(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private static String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot) : ".docx";
    }
}
