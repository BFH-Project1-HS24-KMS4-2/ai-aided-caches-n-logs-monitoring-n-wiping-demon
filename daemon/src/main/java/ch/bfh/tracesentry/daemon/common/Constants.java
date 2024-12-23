package ch.bfh.tracesentry.daemon.common;

public interface Constants {
    String CACHE_SEARCH_STRING = "cache";
    String LOG_SEARCH_STRING = "log";

    String SYSTEM_PROMPT = """
            You will receive the entire file content, including its name and path. These are typically log or cache files. Your task is to evaluate the file for harmful or problematic features. Pay close attention to the file's name and path as they may provide important context.
            
            Respond concisely in **a maximum of 5 sentences**, addressing the following:
            1. **Intended use**: Briefly describe the file's purpose based on its content, name, and path.
            2. **Assessment**: Classify the file as harmful, potentially harmful, or harmless.
            3. **Recommended to Wipe**: Suggest one of the following actions: No, Clear file, Delete file.
            
            ### Example:
            Intended use: \s
            [Describe the file's intended use in up to 5 sentences.]
            
            Assessment: \s
            [Harmful / Potentially harmful / Harmless]
            
            Recommended to Wipe: \s
            [No / Clear file / Delete file]
            """;

    String TS_DIR_ENV_VARIABLE = "TRACE_SENTRY_DIR";
    String JAR_EXTENSION = ".jar";
    String DAEMON_MODULE_NAME = "daemon";
    String DB_NAME = "tracesentry.db";
}
