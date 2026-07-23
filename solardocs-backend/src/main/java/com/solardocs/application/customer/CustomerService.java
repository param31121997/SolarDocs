package com.solardocs.application.customer;

import com.solardocs.application.ports.CustomerIndexRepository;
import com.solardocs.application.ports.CustomerRepository;
import com.solardocs.domain.common.Address;
import com.solardocs.domain.customer.Customer;
import com.solardocs.domain.customer.CustomerId;
import com.solardocs.domain.customer.CustomerStatus;
import com.solardocs.domain.document.GeneratedDocument;
import com.solardocs.domain.document.UploadedDocument;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerIndexRepository indexRepository;
    private final CustomerIdGeneratorService idGenerator;

    public CustomerService(CustomerRepository customerRepository,
                            CustomerIndexRepository indexRepository,
                            CustomerIdGeneratorService idGenerator) {
        this.customerRepository = customerRepository;
        this.indexRepository = indexRepository;
        this.idGenerator = idGenerator;
    }

    public Customer create(String name, String mobile, String email, String aadhaarNumber, Address address) {
        CustomerId id = idGenerator.nextId();
        Customer customer = Customer.newCustomer(id, name, mobile, email, aadhaarNumber, address);
        customerRepository.save(customer);
        reindex(customer);
        return customer;
    }

    public Customer get(String customerId) {
        return customerRepository.findById(new CustomerId(customerId))
                .orElseThrow(() -> new NoSuchElementException("Customer " + customerId + " not found"));
    }

    public Customer update(String customerId, String name, String mobile, String alternateMobile,
                            String email, String aadhaarNumber,
                            Address address, String consumerNumber, String applicationNumber,
                            BigDecimal sanctionedLoadKw, BigDecimal plantCapacityKw,
                            String discom, String category) {
        Customer customer = get(customerId);
        customer.updateMasterData(name, mobile, alternateMobile, email, aadhaarNumber, address, consumerNumber,
                applicationNumber, sanctionedLoadKw, plantCapacityKw, discom, category);
        customerRepository.save(customer);
        reindex(customer);
        return customer;
    }

    public Customer updatePlantDetails(String customerId, com.solardocs.domain.customer.PlantInstallationDetails details) {
        Customer customer = get(customerId);
        customer.updatePlantDetails(details);
        customerRepository.save(customer);
        return customer;
    }

    public Customer updateStatus(String customerId, CustomerStatus status) {
        Customer customer = get(customerId);
        customer.updateStatus(status);
        customerRepository.save(customer);
        reindex(customer);
        return customer;
    }

    /**
     * Removes any previously generated documents matching templateCodes
     * from the customer record, deletes their files from disk, and
     * persists the change. Call this immediately before saving a new
     * generation of the same document(s) so re-generating replaces the
     * old copy instead of accumulating duplicates.
     */
    public void replaceGeneratedDocuments(String customerId, java.util.Set<String> templateCodes) {
        Customer customer = get(customerId);
        var removed = customer.removeGeneratedDocuments(templateCodes);
        customerRepository.save(customer);
        for (var doc : removed) {
            try {
                java.nio.file.Files.deleteIfExists(java.nio.file.Path.of(doc.filePath()));
            } catch (java.io.IOException e) {
                // Not fatal - the DB record is already gone, which is what matters
                // for "no duplicates in the UI"; an orphaned file can be cleaned
                // up later and doesn't block the new generation from proceeding.
                org.slf4j.LoggerFactory.getLogger(CustomerService.class)
                        .warn("Could not delete old generated file {}: {}", doc.filePath(), e.getMessage());
            }
        }
    }

    public Customer addGeneratedDocument(String customerId, GeneratedDocument document) {
        Customer customer = get(customerId);
        customer.addGeneratedDocument(document);
        customerRepository.save(customer);
        return customer;
    }

    public Customer addUploadedDocument(String customerId, UploadedDocument document) {
        Customer customer = get(customerId);
        customer.addUploadedDocument(document);
        customerRepository.save(customer);
        return customer;
    }

    public List<UploadedDocument> listUploadedDocuments(String customerId) {
        return List.copyOf(get(customerId).getUploadedDocuments());
    }

    public void archive(String customerId) {
        Customer customer = get(customerId);
        customer.archive();
        customerRepository.save(customer);
        reindex(customer);
    }

    private void reindex(Customer customer) {
        indexRepository.upsert(new CustomerIndexRepository.IndexEntry(
                customer.getId().value(),
                customer.getName(),
                customer.getMobile(),
                customer.getStatus().name(),
                customer.getId().value() + "-" + customer.getName(),
                customer.getAddress() != null ? customer.getAddress().village() : null,
                customer.getAddress() != null ? customer.getAddress().district() : null,
                customer.getConsumerNumber(),
                customer.getApplicationNumber()
        ));
    }
}
