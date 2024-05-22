package com.cms.incidentmanagement.controller;

import com.cms.core.entity.*;
import com.cms.core.repository.*;
import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Profile("dev")
@Controller
@RequestMapping("/testing")
public class TestController {

	private static final Logger logger = LoggerFactory.getLogger(TestController.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private FeatureRepository featureRepository;

	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private TicketRepository ticketRepository;

	@RequestMapping(method = RequestMethod.GET, value = "/generateData")
	public void dataGenerator() {
		User admin = userRepository.findByUsername("ImsAdmin");
		List<Feature> features = featureRepository.findAll();
		Random random = new Random();
		String[] productTypes = {"hardware", "software"};
		String[] types = {"feature", "support", "bug"};
		String[] priority = {"low", "normal", "high", "urgent", "immediate"};
		String[] status = {"new", "inprogess", "resolved", "feedback"};
		Faker userFaker = new Faker();
		Faker productFaker = new Faker();
		Faker customerFaker = new Faker();
		Faker roleFaker = new Faker();
		Faker deviceFaker = new Faker();
		IntStream.rangeClosed(1, 5000).forEach(item -> {
			try {
				String productName = productFaker.commerce().productName();
				if (productName.length() > 28) {
					productName = productName.substring(0, 28);
				}
				Product product = productRepository.findByName(productName);
				if (product == null) {
					product = new Product();
					product.setName(productName);
					product.setUser(admin);
					product.setProductType(productTypes[random.nextInt(productTypes.length)]);
					product.setDescription(productFaker.lorem().characters(50, 200));
					productRepository.save(product);
				}

				String customerName = customerFaker.company().name();
				if (customerName.length() > 45) {
					customerName = customerName.substring(0, 45);
				}
				Customer customer = customerRepository.findByName(customerName);
				CustomerProductMapping customerProductMapping = null;
				if (customer == null) {
					customer = new Customer();
					customer.setName(customerName);
					customer.setState("kerala");
					customer.setCity(customerFaker.address().cityName());
					customer.setUser(admin);
					Set<CustomerProductMapping> customerProductMappings = new HashSet<>();
					customerProductMapping = new CustomerProductMapping();
					customerProductMapping.setCustomer(customer);
					customerProductMapping.setProduct(product);
					customerProductMappings.add(customerProductMapping);
					customer.setCustomerProductMappings(customerProductMappings);
					customerRepository.save(customer);
				} else {
					for (CustomerProductMapping productMapping : customer.getCustomerProductMappings()) {
						customerProductMapping = productMapping;
						break;
					}
				}

				String roleName = "ROLE_" + roleFaker.funnyName().name().replaceAll(" ", "");
				if (roleName.length() > 18) {
					roleName = roleName.substring(0, 18);
				}
				Role role = roleRepository.findOneByName(roleName);
				if (role == null) {
					role = new Role();
					role.setName(roleName);
					role.setUser(admin);
					Set<RoleFeatureMapping> roleFeatureMappings = new HashSet<>();
					for (Feature feature : features) {
						RoleFeatureMapping roleFeatureMapping = new RoleFeatureMapping();
						roleFeatureMapping.setFeature(feature);
						roleFeatureMapping.setRole(role);
						roleFeatureMappings.add(roleFeatureMapping);
					}
					role.setRoleFeatureMappings(roleFeatureMappings);
					roleRepository.save(role);
				}

				String userName = userFaker.name().username().replace(".", "");
				if (userName.length() > 28) {
					userName = userName.substring(0, 28);
				}
				User user = userRepository.findByUsername(userName);
				if (user == null) {
					user = new User();
					user.setName(userFaker.name().firstName());
					user.setUsername(userName);
					user.setEmail(userName + "@cms.co.in");
					user.setPassword(passwordEncoder.encode("12345"));
					user.setCreatedTime(new Timestamp(System.currentTimeMillis()));
					Set<UserRoleMapping> userRoleMappings = new HashSet<>();
					UserRoleMapping userRoleMapping = new UserRoleMapping();
					userRoleMapping.setRole(role);
					userRoleMapping.setUser(user);
					userRoleMappings.add(userRoleMapping);
					user.setUserRoleMappings(userRoleMappings);
					user.setCustomer(customer);
					user.setUser(admin);
					Set<UserProductMapping> userProductMappings = new HashSet<>();
					UserProductMapping userProductMapping = new UserProductMapping();
					userProductMapping.setUser(user);
					userProductMapping.setCustomerProductMapping(customerProductMapping);
					userProductMappings.add(userProductMapping);
					user.setUserProductMappings(userProductMappings);
					userRepository.save(user);
				}

				String deviceName = deviceFaker.funnyName().name();
				if (deviceName.length() > 28) {
					deviceName = deviceName.substring(0, 28);
				}
				Device device = deviceRepository.findByName(deviceName);
				CustomerProductMappingDevice customerProductMappingDevice = null;
				if (device == null) {
					device = new Device();
					device.setName(deviceName);
					device.setLat(Double.valueOf(deviceFaker.address().latitude()));
					device.setLon(Double.valueOf(deviceFaker.address().longitude()));
					device.setDescription(deviceFaker.lorem().characters(50, 200));
					device.setUser(admin);
					Set<CustomerProductMappingDevice> customerProductMappingDevices = new HashSet<>();
					customerProductMappingDevice = new CustomerProductMappingDevice();
					customerProductMappingDevice.setDevice(device);
					customerProductMappingDevice.setCustomerProductMapping(customerProductMapping);
					customerProductMappingDevices.add(customerProductMappingDevice);
					device.setCustomerProductMappingDevices(customerProductMappingDevices);
					deviceRepository.save(device);
				} else {
					for (CustomerProductMappingDevice productMappingDevice : device.getCustomerProductMappingDevices()) {
						customerProductMappingDevice = productMappingDevice;
						break;
					}
				}

				Faker ticketFaker = new Faker();
				CustomerProductMapping finalCustomerProductMapping = customerProductMapping;
				CustomerProductMappingDevice finalCustomerProductMappingDevice = customerProductMappingDevice;
				User finalUser = user;
				IntStream.rangeClosed(1, 10).forEach(i -> {
					Ticket ticket = new Ticket();
					ticket.setSubject(ticketFaker.lorem().word());
					ticket.setType(types[random.nextInt(types.length)]);
					ticket.setIssueRelated(productTypes[random.nextInt(productTypes.length)]);
					ticket.setPriority(priority[random.nextInt(priority.length)]);
					ticket.setStatus(status[random.nextInt(status.length)]);
					ticket.setDescription(ticketFaker.lorem().characters(30, 150));
					ticket.setCreatedTime(new Timestamp(ticketFaker.date().past(10, TimeUnit.DAYS).getTime()));
					ticket.setCreatedUser(admin);
					ticket.setAssignedTo(admin);
					ticket.setCustomerProductMappingDevice(finalCustomerProductMappingDevice);
					ticket.setCustomerProductMapping(finalCustomerProductMapping);

					Set<TicketFile> ticketFiles = new HashSet<>();
					for (int j = 0; j < 5; j++) {
						TicketFile ticketFile = new TicketFile();
						ticketFile.setTicket(ticket);
						ticketFile.setSubject(ticketFaker.animal().name());
						ticketFile.setFilePath("/images/test1..jfif");
						ticketFile.setFileType(".jfif");
						ticketFiles.add(ticketFile);
					}
					Set<TicketUpdationHistory> ticketUpdationHistory = new HashSet<>();
					for (int j = 0; j < 5; j++) {
						TicketUpdationHistory updationHistory = new TicketUpdationHistory();
						updationHistory.setTicket(ticket);
						updationHistory.setUpdatedBy(finalUser);
						updationHistory.setUpdatedTime(new Timestamp(ticketFaker.date().past(8, TimeUnit.DAYS).getTime()));
						updationHistory.setChangeIn("status");
						updationHistory.setChangeFrom("0");
						updationHistory.setChangeTo((20 * (i + 1)) + "");
						ticketUpdationHistory.add(updationHistory);
					}
					ticket.setTicketFiles(ticketFiles);
					ticket.setTicketUpdationHistory(ticketUpdationHistory);
					Set<Comment> comments = new HashSet<>();
					for (int j = 0; j < 10; j++) {
						Comment comment = new Comment();
						comment.setTicket(ticket);
						comment.setComment(ticketFaker.lorem().characters(20, 150));
						comment.setCreatedTime(new Timestamp(ticketFaker.date().past(6, TimeUnit.DAYS).getTime()));
						Set<CommentsFile> files = new HashSet<>();
						for (int k = 0; k < 3; k++) {
							CommentsFile commentsFile = new CommentsFile();
							commentsFile.setComment(comment);
							commentsFile.setFilePath("/images/test1..jfif");
							commentsFile.setFileType(".jfif");
							files.add(commentsFile);
						}
						comment.setFiles(files);
						comments.add(comment);
					}
					ticket.setComments(comments);
					ticketRepository.save(ticket);
				});

			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		});
	}
}
