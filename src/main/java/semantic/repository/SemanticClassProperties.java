package semantic.repository;

public class SemanticClassProperties {
    private double preference;
    private double confidence;
    private double activation;

    public SemanticClassProperties(double preference, double confidence, double activation) {
        this.preference = preference;
        this.confidence = confidence;
        this.activation = activation;
    }

    public double getPreference() {
        return preference;
    }

    public void setPreference(double preference) {
        this.preference = preference;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double getActivation() {
        return activation;
    }

    public void setActivation(double activation) {
        this.activation = activation;
    }
}
