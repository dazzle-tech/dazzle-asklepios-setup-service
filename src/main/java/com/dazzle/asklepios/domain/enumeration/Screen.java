package com.dazzle.asklepios.domain.enumeration;

public enum Screen {
    ALLERGENS("Allergens", "Allergens setup", Module.SYSTEM_SETUP, "Fa500Px", 10, "allergens"),
    QUICK_APPOINTMENT("Quick Appointment", "", Module.FRONT_DESK_OFFICE, "FaClock", 2, "encounter-registration"),
    POTENTIAL_DUPLICATE("Potential Duplicate", "", Module.SYSTEM_SETUP, "FaInstalod", 15, "potintial-duplicate"),
    PATIENT_REGISTRATION("Patient Registration", "", Module.FRONT_DESK_OFFICE, "FaFilePen", 1, "patient-profile"),
    INFORMATION_DESK("Information Desk", "Search and view registered patients in specific facility", Module.FRONT_DESK_OFFICE, "FaPersonRays", 0, "information-desk"),
    CPT("CPT", "", Module.CODING_MODULE, "FaMicroscope", 0, "cpt-setup"),
    ICD10("ICD-10", "list of ICD-10 codes for diagnosis", Module.CODING_MODULE, "FaStethoscope", 0, "icd10-setup"),
    BRAND_MEDICATIONS("Brand Medications", "", Module.SYSTEM_SETUP, "FaBottleDroplet", 8, "brand-medications"),
    SCHEDULING_SCREEN("Scheduling Screen", "", Module.SCHEDULING, "FaCalendarDays", 0, "schedual-screen"),
    LISTS_OF_VALUE("Lists of Value", "", Module.SYSTEM_SETUP, "FaList", 8, "lov-setup"),
    MODULES_AND_SCREENS("Modules & Screens", "", Module.SYSTEM_SETUP, "FaDesktop", 0, "modules-setup"),
    DATA_VALIDATION_MANAGER("Data Validation Manager", "", Module.SYSTEM_SETUP, "FaDatabase", 1, "dvm"),
    ACCESS_ROLES("Access Roles", "", Module.SYSTEM_SETUP, "FaKey", 2, "access-roles"),
    FACILITIES("Facilities", "", Module.SYSTEM_SETUP, "FaBuilding", 3, "facilities"),
    DEPARTMENTS("Departments", "", Module.SYSTEM_SETUP, "FaHouseMedical", 4, "departments"),
    UOM_GROUP("UOM Group", "", Module.SYSTEM_SETUP, "FaBox", 7, "uom-group"),
    LOINC("LOINC", "", Module.CODING_MODULE, "FaDebian", 0, "loinc-setup"),
    PRESCRIPTION_INSTRUCTIONS("Prescription Instructions", "", Module.SYSTEM_SETUP, "FaPrescriptionBottle", 15, "prescription-instructions"),
    AGE_GROUP("Age Group", "Age Group Setup", Module.SYSTEM_SETUP, "FaBaby", 10, "age-group"),
    PRACTITIONERS("Practitioners", "", Module.SYSTEM_SETUP, "FaBriefcaseMedical", 5, "practitioners"),
    ACTIVE_INGREDIENTS("Active Ingredients", "", Module.SYSTEM_SETUP, "FaPills", 7, "active-ingredients"),
    METADATA("Metadata (View)", "", Module.SYSTEM_SETUP, "FaTags", 0, "metadata"),
    RESOURCES("Resources", "Resources setup", Module.SYSTEM_SETUP, "FaBilibili", 6, "resources"),
    PATIENTS_EMR("Patients EMR", "", Module.CLINICS_MANAGEMENT, "FaFileWaveform", 2, "patient-EMR"),
    INTERNAL_DRUG_ORDERS("Internal Drug Orders", "", Module.PHARMACY, "FaPills", 0, "pharmacy-internal-orders"),
    DENTAL_ACTIONS("Dental Actions", "", Module.SYSTEM_SETUP, "FaTeeth", 9, "dental-actions"),
    USERS("Users", "", Module.SYSTEM_SETUP, "FaUsers", 4, "users"),
    CATALOG_SETUP("Catalog Setup", "Diagnostic tests catalog", Module.SYSTEM_SETUP, "FaBook", 14, "catalog"),
    DIAGNOSTICS_SETUP("Diagnostics Setup", "Define Diagnostic tests (Lab, Rad, Path)", Module.SYSTEM_SETUP, "FaMicroscope", 13, "diagnostics-test"),
    VACCINE_SETUP("Vaccine Setup", "Vaccine", Module.SYSTEM_SETUP, "FaSyringe", 9, "vaccine-setup"),
    CLINICAL_LABORATORY("Clinical Laboratory", "", Module.LABORATORY, "FaFlaskVial", 0, "lab-module"),
    CDT_CODES("CDT Codes", "International dental procedures codes", Module.CODING_MODULE, "FaTooth", 1, "cdt-setup"),
    PATIENTS_VISITS_LIST("Patients Visits List", "", Module.CLINICS_MANAGEMENT, "FaList", 1, "encounter-list"),
    SNOMED_CT("SNOMED-CT", "", Module.CODING_MODULE, "FaDigitalOcean", 0, ""),
    IMAGING_RADIOLOGY("Imaging Radiology", "", Module.RADIOLOGY, "FaSkull", 0, "rad-module"),
    PROCEDURE_SETUP("Procedure Setup", "", Module.SYSTEM_SETUP, "FaProductHunt", 10, "procedure-setup"),
    ROOM_BED_SETUP("Room Bed Setup", "", Module.SYSTEM_SETUP, "FaBed", 16, "room"),
    REVIEW_RESULTS("Review Results", "", Module.CLINICS_MANAGEMENT, "FaRegMessage", 3, "review-results"),
    MEDICATION_MATRIX_SETUP("Medication Matrix Setup", "", Module.SYSTEM_SETUP, "FaMendeley", 12, "med-matrix-setup"),
    INPATIENT_LIST("Inpatient List", "", Module.INPATIENT_CARE, "FaBedPulse", 1, "inpatient-encounters-list"),
    WAITING_LIST("Waiting List", "", Module.INPATIENT_CARE, "FaClock", 0, "waiting-encounters-list"),
    EPRESCRIPTIONS("ePrescriptions", "", Module.PHARMACY, "FaPrescription", 1, "pharmacy-ePrescriptions"),
    INVENTORY_TRANSFER("Inventory Transfer", "", Module.INVENTORY_MANAGEMENT, "FaTrowelBricks", 1, "inventory-transfer"),
    SERVICES_SETUP("Services Setup", "", Module.SYSTEM_SETUP, "FaSprayCanSparkles", 11, "services-setup"),
    PROCEDURE_REQUESTS_LIST("Procedure Requests List", "", Module.PROCEDURES, "FaClipboardUser", 0, "procedure-module"),
    CONTROLLED_MEDICATIONS("Controlled Medications", "", Module.PHARMACY, "FaBiohazard", 2, "pharmacy-controlled-medications"),
    FILES_MERGE("Files Merge", "", Module.FRONT_DESK_OFFICE, "FaFolderTree", 3, "merge-patient-files"),
    OPERATION_REQUESTS("Operation Requests", "", Module.OPERATION_THEATER, "FaOutdent", 1, "operation-module"),
    RECOVERY_ROOM("Recovery Room", "", Module.OPERATION_THEATER, "FaClover", 2, "recovery-module"),
    DAYCASE_PATIENTS_LIST("DayCase Patients List", "", Module.DAY_CASE, "FaPersonWalkingArrowRight", 0, "day-case-list"),
    INVENTORY_TRANSACTIONS("Inventory Transactions", "", Module.INVENTORY_MANAGEMENT, "FaTentArrowLeftRight", 0, "inventory-transaction"),
    OPERATION_ROOM_MATERIALS("Operation Room Materials", "", Module.OPERATION_THEATER, "FaSyringe", 0, "operation-room-materials"),
    INVENTORY_TRANSFER_APPROVAL("Inventory Transfer Approval", "", Module.INVENTORY_MANAGEMENT, "FaBatteryHalf", 2, "inventory-transfer-approval"),
    VACCINE_SCHEDULE_SETUP("Vaccine Schedule Setup", "define vaccine age time", Module.SYSTEM_SETUP, "FaSyringe", 8, "vaccines-schedule-setup"),
    CHECKLISTS("Checklists", "Cheklist", Module.SYSTEM_SETUP, "FaAdn", 5, "checklists"),
    OPERATION_SETUP("Operation Setup", "Major Operations", Module.SYSTEM_SETUP, "FaHeadSideMask", 10, "operation-setup"),
    PURCHASE_APPROVAL("Purchase Approval", "", Module.SYSTEM_SETUP, "FaBoxOpen", 10, "purchase-approvals-setup"),
    QUESTIONNAIRE_SETUP("Questionnaire Setup", "", Module.SYSTEM_SETUP, "FaClipboardQuestion", 5, "questionnaire-setup"),
    MEDICATION_SCHEDULE_SETUP("Medication Schedule Setup", "", Module.SYSTEM_SETUP, "FaPlay", 9, "medication-schedule"),
    DURATION_SETUP("Duration Setup", "", Module.SYSTEM_SETUP, "FaClock", 9, "visit-duration-setup"),
    CLINICAL_PROTOCOLS_SETUP("Clinical Protocols Setup", "where to define clinical protocols dynamically", Module.SYSTEM_SETUP, "FaStethoscope", 1, ""),
    LEDGER_ACCOUNT("Ledger Account", "", Module.BILLING_FINANCE, "FaMobileRetro", 1, ""),
    SHIFT_SETUP("Shift Setup", "", Module.SYSTEM_SETUP, "FaClock", 13, "shift-setup"),
    SUPPLIER_SETUP("Supplier Setup", "Supplier Setup", Module.SYSTEM_SETUP, "FaRedRiver", 15, "supplier-setup"),
    SURGICAL_KITS_SETUP("Surgical Kits Setup", "", Module.SYSTEM_SETUP, "FaCheckToSlot", 10, "surgical-kits-setup"),
    SUPPLIER_MANAGEMENT("Supplier Management", "", Module.PURCHASING, "FaBriefcase", 3, ""),
    QUOTATION_MANAGEMENT("Quotation Management", "", Module.PURCHASING, "FaPenClip", 4, ""),
    PRICE_LISTS("Price Lists", "", Module.BILLING_FINANCE, "FaMoneyBill", 0, "progress-notes"),
    PAYMENT_TRACKING("Payment Tracking", "", Module.PURCHASING, "FaCoins", 7, ""),
    LIST_OF_REQUISITION("List of Requisition", "", Module.PURCHASING, "FaMoneyBills", 5, "list-of-requisition"),
    PURCHASE_RETURN("Purchase Return", "", Module.PURCHASING, "FaArrowLeft", 8, ""),
    USER_NEW("User", "", Module.SYSTEM_SETUP, "FaPersonArrowDownToLine", 0, "users-new"),
    TEST_REPORT_TEMPLATE_SETUP("Test Report Template Setup", "to link test radiology or pathology to specific template", Module.SYSTEM_SETUP, "FaList", 0, "report-result-template"),
    PRODUCTS_CATALOG("Products Catalog", "", Module.INVENTORY_MANAGEMENT, "FaBoxOpen", 3, "product-catalog"),
    MAR("MAR", "", Module.CLINICS_MANAGEMENT, "FaPills", 0, "mar"),
    PURCHASE_REQUISITION("Purchase Requisition", "", Module.PURCHASING, "FaRegPaperPlane", 0, "purchasing-requisition"),
    TELECONSULTATIONS_REQUESTS("TeleConsultations Requests", "", Module.TELEMEDICINE, "FaVideo", 0, "tele-consultation-screen"),
    REQUISITION_APPROVAL("Requisition Approval", "", Module.PURCHASING, "FaRegThumbsUp", 1, ""),
    PURCHASE_ORDERS("Purchase Orders", "", Module.PURCHASING, "FaCubes", 2, ""),
    WAREHOUSE_SETUP("Warehouse Setup", "", Module.INVENTORY_MANAGEMENT, "FaBox", 11, "warehouse-setup"),
    PRODUCTS_SETUP("Products Setup", "setup for products that will be used in warehouse transactions", Module.SYSTEM_SETUP, "FaSitemap", 11, "inventory-product-setup"),
    WAREHOUSE_ITEMS_SETUP("Warehouse Items Setup", "", Module.INVENTORY_MANAGEMENT, "FaBoxArchive", 11, "warehouse-items-setup"),
    DEPARTMENT_STOCK("Department Stock", "", Module.INPATIENT_CARE, "FaPills", 2, "department-stock"),
    GOODS_RECEIPT_NOTE("Goods Receipt Note", "", Module.PURCHASING, "FaBoxTissue", 6, "reset-password"),
    ER_TRIAGE("ER Triage", "", Module.EMERGENCY, "FaCommentMedical", 1, "ER-triage"),
    ER_WAITING_LIST("ER Waiting List", "", Module.EMERGENCY, "FaClock", 2, "ER-waiting-list"),
    ER_DEPARTMENT("ER Department", "", Module.EMERGENCY, "FaBriefcaseMedical", 3, "ER-department"),
    ER_DASHBOARD("ER Dashboard", "", Module.EMERGENCY, "FaChartBar", 0, "ER-dashboard");

    private final String name;
    private final String description;
    private final Module module;
    private final String icon;
    private final int viewOrder;
    private final String navPath;

    Screen(String name, String description, Module module, String icon, int viewOrder, String navPath) {
        this.name = name;
        this.description = description;
        this.module = module;
        this.icon = icon;
        this.viewOrder = viewOrder;
        this.navPath = navPath;
    }

    public Module getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public int getViewOrder() {
        return viewOrder;
    }

    public String getNavPath() {
        return navPath;
    }

}
