package com.solardocs.domain.customer;

import com.solardocs.domain.common.Address;
import com.solardocs.domain.document.GeneratedDocument;
import com.solardocs.domain.document.UploadedDocument;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Customer {

    private final CustomerId id;
    private String name;
    private String mobile;
    private String alternateMobile;
    private Address address;
    private String consumerNumber;
    private String applicationNumber;
    private BigDecimal sanctionedLoadKw;
    private BigDecimal plantCapacityKw;
    private String discom;
    private String category;          // RESIDENTIAL | COMMERCIAL
    private CustomerStatus status;
    private boolean archived;
    private final List<UploadedDocument> uploadedDocuments = new ArrayList<>();
    private final List<GeneratedDocument> generatedDocuments = new ArrayList<>();
    private PlantInstallationDetails plantDetails = PlantInstallationDetails.empty();
    private final Instant createdAt;
    private Instant updatedAt;

    private Customer(CustomerId id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.status = CustomerStatus.LEAD;
        this.archived = false;
    }

    public static Customer newCustomer(CustomerId id, String name, String mobile, Address address) {
        Customer c = new Customer(id, Instant.now());
        c.name = name;
        c.mobile = mobile;
        c.address = address;
        c.plantDetails = PlantInstallationDetails.empty();
        return c;
    }

    public static Customer rehydrate(CustomerId id, String name, String mobile, String alternateMobile,
                                     Address address, String consumerNumber, String applicationNumber,
                                     BigDecimal sanctionedLoadKw, BigDecimal plantCapacityKw,
                                     String discom, String category, CustomerStatus status, boolean archived,
                                     List<UploadedDocument> uploadedDocuments,
                                     List<GeneratedDocument> generatedDocuments,
                                     PlantInstallationDetails plantDetails,
                                     Instant createdAt, Instant updatedAt) {
        Customer customer = new Customer(id, createdAt);
        customer.name = name;
        customer.mobile = mobile;
        customer.alternateMobile = alternateMobile;
        customer.address = address;
        customer.consumerNumber = consumerNumber;
        customer.applicationNumber = applicationNumber;
        customer.sanctionedLoadKw = sanctionedLoadKw;
        customer.plantCapacityKw = plantCapacityKw;
        customer.discom = discom;
        customer.category = category;
        customer.status = status;
        customer.archived = archived;
        customer.uploadedDocuments.addAll(uploadedDocuments == null ? List.of() : uploadedDocuments);
        customer.generatedDocuments.addAll(generatedDocuments == null ? List.of() : generatedDocuments);
        customer.plantDetails = PlantInstallationDetails.orEmpty(plantDetails);
        customer.updatedAt = updatedAt == null ? createdAt : updatedAt;
        return customer;
    }

    public void updateStatus(CustomerStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public void addUploadedDocument(UploadedDocument doc) {
        uploadedDocuments.removeIf(existing -> existing.type().equals(doc.type()));
        uploadedDocuments.add(doc);
        this.updatedAt = Instant.now();
    }

    public void addGeneratedDocument(GeneratedDocument doc) {
        generatedDocuments.add(doc);
        this.updatedAt = Instant.now();
    }

    /**
     * Removes every generated document matching the given template code(s),
     * returning the removed entries so the caller can delete their files
     * from disk. Used right before saving a fresh generation of the same
     * document(s), so re-generating never leaves stale duplicate PDFs
     * behind - there's only ever one current copy of each document.
     */
    public List<GeneratedDocument> removeGeneratedDocuments(java.util.Set<String> templateCodes) {
        List<GeneratedDocument> removed = new ArrayList<>();
        generatedDocuments.removeIf(d -> {
            boolean match = templateCodes.contains(d.templateCode());
            if (match) removed.add(d);
            return match;
        });
        if (!removed.isEmpty()) this.updatedAt = Instant.now();
        return removed;
    }

    public void archive() {
        this.archived = true;
        this.updatedAt = Instant.now();
    }

    /** Replaces the installation/technical details used by the compliance document set - see PlantInstallationDetails. */
    public void updatePlantDetails(PlantInstallationDetails plantDetails) {
        this.plantDetails = PlantInstallationDetails.orEmpty(plantDetails);
        this.updatedAt = Instant.now();
    }

    public void updateMasterData(String name, String mobile, String alternateMobile, Address address,
                                  String consumerNumber, String applicationNumber,
                                  BigDecimal sanctionedLoadKw, BigDecimal plantCapacityKw,
                                  String discom, String category) {
        this.name = name;
        this.mobile = mobile;
        this.alternateMobile = alternateMobile;
        this.address = address;
        this.consumerNumber = consumerNumber;
        this.applicationNumber = applicationNumber;
        this.sanctionedLoadKw = sanctionedLoadKw;
        this.plantCapacityKw = plantCapacityKw;
        this.discom = discom;
        this.category = category;
        this.updatedAt = Instant.now();
    }

    // --- getters (no setters besides the behavior methods above) ---
    public CustomerId getId() { return id; }
    public String getName() { return name; }
    public String getMobile() { return mobile; }
    public String getAlternateMobile() { return alternateMobile; }
    public Address getAddress() { return address; }
    public String getConsumerNumber() { return consumerNumber; }
    public String getApplicationNumber() { return applicationNumber; }
    public BigDecimal getSanctionedLoadKw() { return sanctionedLoadKw; }
    public BigDecimal getPlantCapacityKw() { return plantCapacityKw; }
    public String getDiscom() { return discom; }
    public String getCategory() { return category; }
    public CustomerStatus getStatus() { return status; }
    public boolean isArchived() { return archived; }
    public List<UploadedDocument> getUploadedDocuments() { return uploadedDocuments; }
    public List<GeneratedDocument> getGeneratedDocuments() { return generatedDocuments; }
    public PlantInstallationDetails getPlantDetails() { return plantDetails; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
