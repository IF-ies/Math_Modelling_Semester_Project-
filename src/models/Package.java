package models;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║           SwiftRoute Logistics — Package Model                 ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * Represents a single package in the urban logistics system.
 * Each package has a unique ID, a destination neighborhood,
 * a priority level, and a weight in kilograms.
 *
 * @author SwiftRoute Development Team
 */
public class Package {

    // ─── Fields ──────────────────────────────────────────────────────
    private String packageID;
    private String destination;
    private int priority;       // 1 = Highest, 5 = Lowest
    private double weightKg;

    // ─── Constructors ────────────────────────────────────────────────

    /**
     * Full constructor.
     *
     * @param packageID   unique identifier (e.g., "PKG-001")
     * @param destination delivery neighborhood / address
     * @param priority    delivery priority (1-5)
     * @param weightKg    weight of the package in kilograms
     */
    public Package(String packageID, String destination, int priority, double weightKg) {
        this.packageID = packageID;
        this.destination = destination;
        this.priority = priority;
        this.weightKg = weightKg;
    }

    /**
     * Simplified constructor — priority defaults to 3 (normal),
     * weight defaults to 1.0 kg.
     *
     * @param packageID   unique identifier
     * @param destination delivery neighborhood / address
     */
    public Package(String packageID, String destination) {
        this(packageID, destination, 3, 1.0);
    }

    // ─── Getters & Setters ───────────────────────────────────────────

    public String getPackageID() {
        return packageID;
    }

    public void setPackageID(String packageID) {
        this.packageID = packageID;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    // ─── Display ─────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("[%s] → %s | Priority: %d | Weight: %.1f kg",
                packageID, destination, priority, weightKg);
    }
}
