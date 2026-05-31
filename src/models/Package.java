package models;

/**
 * Veteran Logistics — Package Model
 *
 * Represents a single package in the Kayseri urban logistics system.
 * Each package has a unique ID, a destination neighborhood,
 * a priority level, and a weight in kilograms.
 *
 * @author Veteran Development Team
 */
public class Package {

    // ─── Fields ──────────────────────────────────────────────────────
    private String packageID;
    private String destination;
    private int priority;       // 1 = En Yuksek, 5 = En Dusuk
    private double weightKg;

    // ─── Constructors ────────────────────────────────────────────────

    /**
     * Full constructor.
     *
     * @param packageID   unique identifier (e.g., "PKG_KYS_001")
     * @param destination delivery neighborhood (e.g., "Talas")
     * @param priority    delivery priority (1-5)
     * @param weightKg    weight of the package in kilograms
     */
    public Package(String packageID, String destination, int priority, double weightKg) {
        this.packageID   = packageID;
        this.destination = destination;
        this.priority    = priority;
        this.weightKg    = weightKg;
    }

    /**
     * Simplified constructor — priority defaults to 3 (normal), weight to 1.0 kg.
     */
    public Package(String packageID, String destination) {
        this(packageID, destination, 3, 1.0);
    }

    // ─── Getters & Setters ───────────────────────────────────────────
    public String getPackageID()   { return packageID; }
    public void   setPackageID(String packageID) { this.packageID = packageID; }

    public String getDestination() { return destination; }
    public void   setDestination(String destination) { this.destination = destination; }

    public int    getPriority()    { return priority; }
    public void   setPriority(int priority) { this.priority = priority; }

    public double getWeightKg()    { return weightKg; }
    public void   setWeightKg(double weightKg) { this.weightKg = weightKg; }

    // ─── Display ─────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format("[%s] → %s | Oncelik: %d | Agirlik: %.1f kg",
                packageID, destination, priority, weightKg);
    }
}
