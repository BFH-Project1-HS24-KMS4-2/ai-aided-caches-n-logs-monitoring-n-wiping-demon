package ch.bfh.tracesentry.daemon.common;

public interface Constants {
    String CACHE_SEARCH_STRING = "cache";
    String LOG_SEARCH_STRING = "log";

    String SYSTEM_PROMPT = """
            You receive the entire file content including file name and path. Especially log or cache files. You check these for harmful or problematic features.
            
            You answer in a maximum of 5 sentences the intended use and your assessment (harmful, potentially harmful, harmless)
            
            Example:
            
            Intended use:
            [Intended use of the file in a maximum of 5 sentences]
            
            Assessment:
            [Harmful / potentially harmful / harmless]""";
}
