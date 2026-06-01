package cn.welsione.jprompt.loader;

import cn.welsione.jprompt.TemplateException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * 从文件系统加载模板。
 */
public class FileSystemTemplateLoader implements TemplateLoader {

    private final Path root;
    private final Charset charset;

    public FileSystemTemplateLoader(Path root) {
        this(root, StandardCharsets.UTF_8);
    }

    public FileSystemTemplateLoader(Path root, Charset charset) {
        this.root = Objects.requireNonNull(root, "root must not be null").toAbsolutePath().normalize();
        this.charset = Objects.requireNonNull(charset, "charset must not be null");
    }

    @Override
    public String load(String path) throws TemplateException {
        Path templatePath = resolve(path);
        if (!Files.isRegularFile(templatePath)) {
            throw new TemplateException("找不到提示词模板文件: " + templatePath);
        }
        try {
            return Files.readString(templatePath, charset);
        } catch (IOException e) {
            throw new TemplateException("加载提示词模板失败: " + templatePath, e);
        }
    }

    private Path resolve(String path) {
        Objects.requireNonNull(path, "path must not be null");
        Path resolved = root.resolve(path).normalize();
        if (!resolved.startsWith(root)) {
            throw new TemplateException("模板路径不能超出根目录: " + path);
        }
        return resolved;
    }
}
