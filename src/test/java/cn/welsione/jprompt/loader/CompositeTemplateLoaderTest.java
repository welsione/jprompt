package cn.welsione.jprompt.loader;

import cn.welsione.jprompt.TemplateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CompositeTemplateLoader 功能测试
 */
class CompositeTemplateLoaderTest {

    @Test
    void primarySucceedsReturnsPrimaryResult() {
        CompositeTemplateLoader loader = new CompositeTemplateLoader(
                path -> "primary-content",
                path -> "fallback-content"
        );
        assertEquals("primary-content", loader.load("test.md"));
    }

    @Test
    void primaryFailsFallbackSucceedsReturnsFallbackResult() {
        CompositeTemplateLoader loader = new CompositeTemplateLoader(
                path -> { throw new TemplateException("primary failed"); },
                path -> "fallback-content"
        );
        assertEquals("fallback-content", loader.load("test.md"));
    }

    @Test
    void bothFailThrowsCompositeException() {
        CompositeTemplateLoader loader = new CompositeTemplateLoader(
                path -> { throw new TemplateException("primary error"); },
                path -> { throw new TemplateException("fallback error"); }
        );
        TemplateException ex = assertThrows(TemplateException.class, () -> loader.load("test.md"));
        assertTrue(ex.getMessage().contains("primary error"));
        assertTrue(ex.getMessage().contains("fallback error"));
        assertTrue(ex.getMessage().contains("test.md"));
    }

    @Test
    void nullPrimaryThrowsNPE() {
        assertThrows(NullPointerException.class, () ->
                new CompositeTemplateLoader(null, path -> "content"));
    }

    @Test
    void nullFallbackThrowsNPE() {
        assertThrows(NullPointerException.class, () ->
                new CompositeTemplateLoader(path -> "content", null));
    }

    @Test
    void nullPathThrowsNPE() {
        CompositeTemplateLoader loader = new CompositeTemplateLoader(
                path -> "content", path -> "content"
        );
        assertThrows(NullPointerException.class, () -> loader.load(null));
    }
}
