package com.dazzle.asklepios.domain.enumeration;

public enum Module {

    SCHEDULING("Scheduling", "", "FaCalendarDay", 3),
    INVENTORY_MANAGEMENT("Inventory Management", "", "FaBoxesPacking", 12),
    BILLING_FINANCE("Billing & Finance", "", "FaMoneyBill1", 12),
    SYSTEM_SETUP("System Setup", "", "FaWrench", 0),
    FRONT_DESK_OFFICE("Front Desk Office", null, "FaRegIdBadge", 2),
    EMERGENCY("Emergency", "", "FaExplosion", 6),
    TESTING_MODULE("Testing Module", "", "FaMitten", 13),
    PURCHASING("Purchasing", "", "FaMobileRetro", 13),
    TELEMEDICINE("Telemedicine", "", "FaVideo", 4),
    INPATIENT_CARE("Inpatient Care", "", "FaBed", 5),
    DAY_CASE("Day Case", "", "FaPersonShelter", 6),
    PROCEDURES("Procedures", "", "FaSquareParking", 7),
    OPERATION_THEATER("Operation Theater", "", "FaHeartPulse", 8),
    CLINICS_MANAGEMENT("Clinics Management", null, "FaStethoscope", 4),
    LABORATORY("Laboratory", "", "FaFlask", 9),
    RADIOLOGY("Radiology", "", "FaXRay", 10),
    PHARMACY("Pharmacy", "", "FaPrescriptionBottleMedical", 11),
    CODING_MODULE("Coding Module", "Coding Module", "FaCodepen", 1);

    private final String name;
    private final String description;
    private final String iconImagePath;
    private final int viewOrder;

    Module(String name, String description, String iconImagePath, int viewOrder) {
        this.name = name;
        this.description = description;
        this.iconImagePath = iconImagePath;
        this.viewOrder = viewOrder;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIconImagePath() {
        return iconImagePath;
    }

    public int getViewOrder() {
        return viewOrder;
    }
}
