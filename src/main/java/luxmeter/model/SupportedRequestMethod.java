package luxmeter.model;

/**
 * Allows you to see on first sight which requests are supported.
 * Additionally you don't need to bother handling with strings and case sensitivity.
 */
public enum SupportedRequestMethod {
    GET,
    HEAD;

    /**
     * @param method name of the request method
     * @return equivalent SupportedRequestMethod otherwise null
     */
    public static SupportedRequestMethod of(String method) {
        if (method != null) {
            try {
                return SupportedRequestMethod.valueOf(method.toUpperCase());
            }
            catch (IllegalArgumentException e) {
                // proceed with null since it is expected by the caller
            }
        }
        return null;
    }
}
