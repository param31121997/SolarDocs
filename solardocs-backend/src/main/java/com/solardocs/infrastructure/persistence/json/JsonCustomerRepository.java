package com.solardocs.infrastructure.persistence.json;

import com.solardocs.application.ports.CustomerRepository;
import com.solardocs.config.AppDataDirectoryConfig;
import com.solardocs.domain.common.Address;
import com.solardocs.domain.customer.Customer;
import com.solardocs.domain.customer.CustomerId;
import com.solardocs.domain.customer.CustomerStatus;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public class JsonCustomerRepository implements CustomerRepository {

    private final AppDataDirectoryConfig dirs;
    private final JsonFileUtils json;
    private final FileLockManager locks;

    public JsonCustomerRepository(AppDataDirectoryConfig dirs, JsonFileUtils json, FileLockManager locks) {
        this.dirs = dirs;
        this.json = json;
        this.locks = locks;
    }

    private Path folderFor(CustomerId id, String name) {
        String sanitized = name == null ? "customer" : name.replaceAll("[\\\\/:*?\"<>|]", "").trim();
        if (sanitized.length() > 40) sanitized = sanitized.substring(0, 40);
        return dirs.customersDir().resolve(id.value() + "-" + sanitized.replace(' ', '-'));
    }

    /** The folder name may drift from the current customer name over time — we locate by CustomerId prefix. */
    private Optional<Path> existingFolder(CustomerId id) {
        if (Files.notExists(dirs.customersDir())) return Optional.empty();
        try (Stream<Path> children = Files.list(dirs.customersDir())) {
            return children.filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith(id.value() + "-"))
                    .findFirst();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Customer customer) {
        Path folder = existingFolder(customer.getId()).orElseGet(() -> folderFor(customer.getId(), customer.getName()));
        Path customerFile = folder.resolve("customer.json");
        locks.withLock(customerFile, () -> {
            try {
                Files.createDirectories(folder.resolve("Documents"));
                Files.createDirectories(folder.resolve("Images"));
                Files.createDirectories(folder.resolve("GeneratedPDF"));
                Files.createDirectories(folder.resolve("Notes"));

                var record = new CustomerJsonRecord(
                        customer.getId().value(), customer.getName(), customer.getMobile(),
                        customer.getAlternateMobile(), customer.getEmail(), customer.getAadhaarNumber(),
                        customer.getAddress() != null ? customer.getAddress().addressLine() : null,
                        customer.getAddress() != null ? customer.getAddress().village() : null,
                        customer.getAddress() != null ? customer.getAddress().district() : null,
                        customer.getAddress() != null ? customer.getAddress().state() : null,
                        customer.getAddress() != null ? customer.getAddress().pincode() : null,
                        customer.getConsumerNumber(), customer.getApplicationNumber(),
                        customer.getSanctionedLoadKw(), customer.getPlantCapacityKw(),
                        customer.getDiscom(), customer.getCategory(),
                        customer.getStatus().name(), customer.isArchived(),
                        customer.getUploadedDocuments().stream()
                                .map(d -> new CustomerJsonRecord.UploadedDocRecord(d.id(), d.type(), d.fileName(), d.filePath(), d.uploadedAt()))
                                .toList(),
                        customer.getGeneratedDocuments().stream()
                                .map(d -> new CustomerJsonRecord.GeneratedDocRecord(d.id(), d.templateCode(), d.templateVersion(), d.filePath(), d.generatedAt()))
                                .toList(),
                        toRecord(customer.getPlantDetails()),
                        customer.getCreatedAt(), customer.getUpdatedAt()
                );
                json.writeAtomic(customerFile, record);
                return null;
            } catch (IOException e) {
                throw new RuntimeException("Failed to save customer " + customer.getId().value(), e);
            }
        });
    }

    @Override
    public Optional<Customer> findById(CustomerId id) {
        Optional<Path> folder = existingFolder(id);
        if (folder.isEmpty()) return Optional.empty();
        try {
            CustomerJsonRecord rec = json.read(folder.get().resolve("customer.json"), CustomerJsonRecord.class);
            if (rec == null) return Optional.empty();
            return Optional.of(toDomain(rec));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsById(CustomerId id) {
        return existingFolder(id).isPresent();
    }

    private Customer toDomain(CustomerJsonRecord rec) {
        return Customer.rehydrate(
                new CustomerId(rec.customerId()), rec.name(), rec.mobile(), rec.alternateMobile(),
                rec.email(), rec.aadhaarNumber(),
                new Address(rec.addressLine(), rec.village(), rec.district(), rec.state(), rec.pincode()),
                rec.consumerNumber(), rec.applicationNumber(), rec.sanctionedLoadKw(), rec.plantCapacityKw(),
                rec.discom(), rec.category(), CustomerStatus.valueOf(rec.status()), rec.archived(),
                rec.uploadedDocuments() == null ? java.util.List.of() : rec.uploadedDocuments().stream()
                        .map(d -> new com.solardocs.domain.document.UploadedDocument(
                                d.id(), d.type(), d.fileName(), d.filePath(), d.uploadedAt()))
                        .toList(),
                rec.generatedDocuments() == null ? java.util.List.of() : rec.generatedDocuments().stream()
                        .map(d -> new com.solardocs.domain.document.GeneratedDocument(
                                d.id(), d.templateCode(), d.templateVersion(), d.filePath(), d.generatedAt()))
                        .toList(),
                toDomain(rec.plantDetails()),
                rec.createdAt(), rec.updatedAt()
        );
    }

    private static CustomerJsonRecord.PlantDetailsRecord toRecord(com.solardocs.domain.customer.PlantInstallationDetails d) {
        d = com.solardocs.domain.customer.PlantInstallationDetails.orEmpty(d);
        return new CustomerJsonRecord.PlantDetailsRecord(
                d.installationDate(), d.inverterMake(), d.inverterRating(),
                d.inverterCapacityKw(), d.chargeControllerType(), d.hpd(),
                d.earthing1Ohms(), d.earthing2Ohms(), d.earthing3Ohms(),
                d.moduleWattage(), d.moduleCount(), d.moduleCapacityKw(), d.moduleSerialNumbers(),
                d.cellManufacturerName(), d.cellGstInvoiceNo(),
                d.inspectionDate(), d.inspectionLetterNo(), d.inspectionLetterDate(),
                d.agreementPlace(), d.netMeterSerialNo()
        );
    }

    private static com.solardocs.domain.customer.PlantInstallationDetails toDomain(CustomerJsonRecord.PlantDetailsRecord r) {
        if (r == null) return com.solardocs.domain.customer.PlantInstallationDetails.empty();
        return new com.solardocs.domain.customer.PlantInstallationDetails(
                r.installationDate(), r.inverterMake(), r.inverterRating(),
                r.inverterCapacityKw(), r.chargeControllerType(), r.hpd(),
                r.earthing1Ohms(), r.earthing2Ohms(), r.earthing3Ohms(),
                r.moduleWattage(), r.moduleCount(), r.moduleCapacityKw(), r.moduleSerialNumbers(),
                r.cellManufacturerName(), r.cellGstInvoiceNo(),
                r.inspectionDate(), r.inspectionLetterNo(), r.inspectionLetterDate(),
                r.agreementPlace(), r.netMeterSerialNo()
        );
    }
}
