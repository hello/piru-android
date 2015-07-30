package is.hello.piru.api.model;

public enum FirmwareType {
    SENSE("morpheus"),
    PILL("pill");

    private final String value;

    FirmwareType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
