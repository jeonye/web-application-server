package constants;

public enum HttpMethod {
    GET
    , POST;

    public boolean isPOST() {
        return this == POST;
    }

    public boolean isGET() {
        return this == GET;
    }
}
