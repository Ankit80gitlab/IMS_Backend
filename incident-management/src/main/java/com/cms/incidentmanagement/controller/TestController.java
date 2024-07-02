package com.cms.incidentmanagement.controller;

import com.cms.core.entity.*;
import com.cms.core.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.ProjCoordinate;
import org.osgeo.proj4j.proj.Projection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private AreaRepository areaRepository;
    @Autowired
    private UserTypeRepository userTypeRepository;
    @Autowired
    private IncidentTypeRepository incidentTypeRepository;

    @Autowired
    private AreaDeviceMappingRepository areaDeviceMappingRepository;

    private static final double ZONE_RADIUS_MIN = 25000;

    private static final double ZONE_RADIUS_MAX = 40000;

    private static final double AREA_RADIUS = 4000;

    private static final int POINTS_PER_POLYGON_MIN = 4;

    private static final int POINTS_PER_POLYGON_MAX = 5;

    @RequestMapping(method = RequestMethod.GET, value = "/generateData")
    public void dataGenerator() {
        ObjectMapper objectMapper = new ObjectMapper();
        CRSFactory factory = new CRSFactory();
        CoordinateReferenceSystem crs = factory.createFromName("EPSG:3857");
        Projection projection = crs.getProjection();
        User admin = userRepository.findByUsername("ImsAdmin");
        List<Feature> features = featureRepository.findAll();
        List<UserType> userTypes = userTypeRepository.findAll();
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
        Faker zoneFaker = new Faker();
        Faker areaFaker = new Faker();
        Faker incidentTypeFaker = new Faker();
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
                    Set<IncidentType> incidentTypes = new HashSet<>();
                    for (int l = 0; l < 5; l++) {
                        IncidentType incidentType = new IncidentType();
                        incidentType.setProduct(product);
                        incidentType.setType(types[random.nextInt(types.length)]);
                        incidentType.setDescription(incidentTypeFaker.lorem().characters(50, 100));
                        incidentType.setUser(admin);
                        incidentTypes.add(incidentType);
                    }
                    product.setIncidentTypes(incidentTypes);
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
                    UserType userType = userTypes.get(random.nextInt(userTypes.size()));
                    user.setUserType(userType);
                    double[] randomPoint = getRandomPointInIndia(random);
                    user.setLat(randomPoint[1]);
                    user.setLon(randomPoint[0]);
                    Set<UserProductMapping> userProductMappings = new HashSet<>();
                    UserProductMapping userProductMapping = new UserProductMapping();
                    userProductMapping.setUser(user);
                    userProductMapping.setCustomerProductMapping(customerProductMapping);
                    userProductMappings.add(userProductMapping);
                    user.setUserProductMappings(userProductMappings);
                    userRepository.save(user);
                }
                String zoneName = zoneFaker.address().cityName();
                if (zoneName.length() > 28) {
                    zoneName = zoneName.substring(0, 28);
                }
                Zone zone = zoneRepository.findByName(zoneName);
                if (zone == null) {
                    double[] zoneCenter = getRandomPointInIndia(random);
                    double zoneRadius = ZONE_RADIUS_MIN + (ZONE_RADIUS_MAX - ZONE_RADIUS_MIN) * random.nextDouble();
                    int zonePoints = POINTS_PER_POLYGON_MIN + random.nextInt(POINTS_PER_POLYGON_MAX - POINTS_PER_POLYGON_MIN + 1);
                    List<double[]> zonePolygon = generatePolygon(zoneCenter, zoneRadius, zonePoints);
                    String zonePolygonString = "[" + objectMapper.writeValueAsString(zonePolygon) + "]";
                    zone = new Zone();
                    zone.setUser(admin);
                    zone.setCustomer(customer);
                    zone.setCreatedTime(new Timestamp(System.currentTimeMillis()));
                    zone.setName(zoneName);
                    zone.setPolygon(zonePolygonString);
                    zoneRepository.save(zone);
                    for (int j = 0; j < 4; j++) {
                        double[] areaCenter = getRandomPointInRadius(zoneCenter, zoneRadius - AREA_RADIUS, random, projection);
                        List<double[]> areaPolygon = generatePolygon(areaCenter, AREA_RADIUS, zonePoints);
                        String areaPolygonString = "[" + objectMapper.writeValueAsString(areaPolygon) + "]";
                        String areaName = areaFaker.address().streetName();
                        if (areaName.length() > 28) {
                            areaName = areaName.substring(0, 28);
                        }
                        Area area = new Area();
                        area.setName(areaName);
                        area.setPolygon(areaPolygonString);
                        area.setUser(admin);
                        area.setZone(zone);
                        area.setCreatedTime(new Timestamp(System.currentTimeMillis()));
                        Set<AreaUserMapping> areaUserMappings = new HashSet<>();
                        AreaUserMapping areaUserMapping = new AreaUserMapping();
                        areaUserMapping.setArea(area);
                        areaUserMapping.setUser(user);
                        areaUserMappings.add(areaUserMapping);
                        area.setAreaUserMappings(areaUserMappings);
                        areaRepository.save(area);
                        Random rand = new Random();
                        List<double[]> randomPoints = generateRandomPointsInPolygon(areaPolygon, 50, rand, projection);
                        for (double[] point : randomPoints) {
                            String deviceName = deviceFaker.funnyName().name();
                            if (deviceName.length() > 28) {
                                deviceName = deviceName.substring(0, 28);
                            }

                            Device device = new Device();
                            device.setName(deviceName);
                            device.setUid(deviceName);
                            device.setLon(point[0]);
                            device.setLat(point[1]);
                            device.setDescription(deviceFaker.lorem().characters(50, 200));
                            device.setUser(admin);
                            CustomerProductMappingDevice customerProductMappingDevice = new CustomerProductMappingDevice();
                            customerProductMappingDevice.setDevice(device);
                            customerProductMappingDevice.setCustomerProductMapping(customerProductMapping);
                            device.setCustomerProductMappingDevice(customerProductMappingDevice);
                            deviceRepository.save(device);
                            AreaDeviceMapping areaDeviceMapping = new AreaDeviceMapping();
                            areaDeviceMapping.setArea(area);
                            areaDeviceMapping.setDevice(device);
                            areaDeviceMappingRepository.save(areaDeviceMapping);
                            Faker ticketFaker = new Faker();
                            CustomerProductMapping finalCustomerProductMapping = customerProductMapping;
                            User finalUser = user;
                            IntStream.rangeClosed(1, 10).forEach(k -> {
                                Ticket ticket = new Ticket();
                                ticket.setSubject(ticketFaker.lorem().word());
                                List<IncidentType> incidentTypes1 = incidentTypeRepository.findAll();
                                IncidentType incidentType = incidentTypes1.get(random.nextInt(incidentTypes1.size()));
                                ticket.setIncidentType(incidentType);
                                ticket.setIssueRelated(productTypes[random.nextInt(productTypes.length)]);
                                ticket.setPriority(priority[random.nextInt(priority.length)]);
                                ticket.setStatus(status[random.nextInt(status.length)]);
                                ticket.setDescription(ticketFaker.lorem().characters(30, 150));
                                ticket.setCreatedTime(new Timestamp(ticketFaker.date().past(10, TimeUnit.DAYS).getTime()));
                                ticket.setCreatedUser(admin);
                                ticket.setIsDuplicate(false);
                                ticket.setAssignedTo(admin);
                                ticket.setUpdatedBy(finalUser);
                                ticket.setCustomerProductMappingDevice(customerProductMappingDevice);
                                ticket.setCustomerProductMapping(finalCustomerProductMapping);
                                Set<TicketFile> ticketFiles = new HashSet<>();
                                for (int l = 0; l < 5; l++) {
                                    String fileName = ticketFaker.artist().name();
                                    TicketFile ticketFile = new TicketFile();
                                    ticketFile.setTicket(ticket);
                                    ticketFile.setOptionalDescription(ticketFaker.animal().name());
                                    ticketFile.setFileName(fileName);
                                    ticketFile.setFilePath("/images/");
                                    ticketFile.setFileType(".jpeg");
                                    ticketFiles.add(ticketFile);
                                }
                                Set<TicketUpdationHistory> ticketUpdationHistory = new HashSet<>();
                                for (int l = 0; l < 5; l++) {
                                    TicketUpdationHistory updationHistory = new TicketUpdationHistory();
                                    updationHistory.setTicket(ticket);
                                    updationHistory.setUpdatedBy(finalUser);
                                    updationHistory.setUpdatedTime(new Timestamp(ticketFaker.date().past(8, TimeUnit.DAYS).getTime()));
                                    updationHistory.setChangeIn("status");
                                    updationHistory.setChangeFrom("0");
                                    updationHistory.setChangeTo((20 * (l + 1)) + "");
                                    ticketUpdationHistory.add(updationHistory);
                                }
                                ticket.setTicketFiles(ticketFiles);
                                ticket.setTicketUpdationHistory(ticketUpdationHistory);
                                Set<Comment> comments = new HashSet<>();
                                for (int l = 0; l < 10; l++) {
                                    Set<TicketFile> commentFileList = new HashSet<>();
                                    Comment comment = new Comment();
                                    comment.setTicket(ticket);
                                    comment.setComment(ticketFaker.lorem().characters(20, 150));
                                    comment.setUser(admin);
                                    comment.setCreatedTime(new Timestamp(ticketFaker.date().past(6, TimeUnit.DAYS).getTime()));
                                    for (int m = 0; m < 2; m++) {
                                        String fileName = ticketFaker.file().fileName();
                                        TicketFile commentFile = new TicketFile();
                                        commentFile.setTicket(ticket);
                                        commentFile.setOptionalDescription(ticketFaker.animal().name());
                                        commentFile.setFileName(fileName);
                                        commentFile.setFilePath("/images/");
                                        commentFile.setFileType(".pdf");
                                        commentFile.setComment(comment);
                                        ticketFiles.add(commentFile);
                                        commentFileList.add(commentFile);
                                    }
                                    comment.setTicketFiles(commentFileList);
                                    comments.add(comment);
                                }
                                ticket.setComments(comments);
                                ticketRepository.save(ticket);
                            });
                        }
                    }
                }

            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        });
    }

    private static double[] getRandomPointInIndia(Random random) {
        // Approximate bounding box for India in EPSG:3857
        double minX = 6800000;
        double maxX = 10000000;
        double minY = 600000;
        double maxY = 4000000;

        double x = minX + (maxX - minX) * random.nextDouble();
        double y = minY + (maxY - minY) * random.nextDouble();

        return new double[]{x, y};
    }

    private static double[] getRandomPointInRadius(double[] center, double radius, Random random, Projection projection) {
        double angle = 2 * Math.PI * random.nextDouble();
        double distance = radius * random.nextDouble();
        double dx = distance * Math.cos(angle);
        double dy = distance * Math.sin(angle);

        return new double[]{center[0] + dx, center[1] + dy};
    }

    private static List<double[]> generatePolygon(double[] center, double radius, int points) {
        List<double[]> polygon = new ArrayList<>();
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double dx = radius * Math.cos(angle);
            double dy = radius * Math.sin(angle);
            polygon.add(new double[]{center[0] + dx, center[1] + dy});
        }
        // Close the polygon by adding the first point at the end
        polygon.add(polygon.get(0));
        return polygon;
    }

    private static List<double[]> generateRandomPointsInPolygon(List<double[]> coordinates, int numberOfPoints, Random rand, Projection proj3857) {
        List<double[]> arrayPoints = new ArrayList<>();
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (double[] coord : coordinates) {
            if (coord[0] < minX) minX = coord[0];
            if (coord[0] > maxX) maxX = coord[0];
            if (coord[1] < minY) minY = coord[1];
            if (coord[1] > maxY) maxY = coord[1];
        }

        for (int i = 0; i < numberOfPoints; i++) {
            double randomX = minX + (maxX - minX) * rand.nextDouble();
            double randomY = minY + (maxY - minY) * rand.nextDouble();
            ProjCoordinate srcCoord = new ProjCoordinate(randomX, randomY);
            ProjCoordinate dstCoord = new ProjCoordinate();
            proj3857.inverseProject(srcCoord, dstCoord);
            double[] array = new double[2];
            array[0] = dstCoord.y;
            array[1] = dstCoord.x;
            arrayPoints.add(array);
        }
        return arrayPoints;
    }
}
