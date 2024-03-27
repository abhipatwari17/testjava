package com.example.thoughtclan.conversion.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.thoughtclan.conversion.entity.*;
import com.example.thoughtclan.conversion.repository.*;
import com.example.thoughtclan.conversion.service.ExtractionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Service
public class ExtractionServiceImpl implements ExtractionService {
	
	@Autowired
	private ToolsRepository toolsRepository;
	
	@Autowired
	private SASTOverviewRepository sastOverviewRepository;
	
	@Autowired
	private SASTScanRepository sastScanRepository;
	
	@Autowired
	private SASTVulnerabilitiesRepository sastVulnerabilitiesRepository;
	
	@Autowired
	private ApplicationRepository applicationRepository;
	
	@Autowired
	private BitbucketRepoRepository bitbucketRepoRepository;
	
	@Autowired
	private BranchRepository branchRepository;

	@Override
	public Object parcingTheXmlFile(byte[] inputstream, String fileName) {
		try {
			
			// Sample file name format is application_name:repository_name:branch_name:{UUID}
			String[] fileNameDetails = fileName.split(":");
			if(fileNameDetails.length > 4) {
				String appName = fileNameDetails[0];
				String repoName = fileNameDetails[1];
				String branchName = fileNameDetails[0];
			}
			
			List<Tools> tools = toolsRepository.findAll();
			Tools tool = new Tools();
			if(tools.isEmpty()) {
				tool.setName("veracode");
				tool = toolsRepository.save(tool);
			} else {
				tool = tools.get(0);
			}
			
			Optional<Application> optionalApplication = applicationRepository.findByApplicationKey("application one");
			Application application = new Application();
			if(!optionalApplication.isPresent()) {
				application.setName("application one");
				application = applicationRepository.save(application);
			} else {
				application = optionalApplication.get();
			}
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
			SASTScan sastScan = new SASTScan();
			XmlMapper xmlMapper = new XmlMapper();
			JsonNode rootNode = xmlMapper.readTree(inputstream);
			
			// Checking the existing sast scan based on scan id and build id
			sastScan.setScanId(rootNode.has("analysis_id") ? rootNode.get("analysis_id").asText() : null);
			sastScan.setBuildId(rootNode.has("build_id") ? rootNode.get("build_id").asText() : null);
			sastScan.setScannedOn(rootNode.has("generation_date") ? LocalDateTime.parse(rootNode.get("generation_date").asText(), formatter) : null);
			List<SASTScan> existingSastScans = sastScanRepository.findByScanIdAndBuildId(sastScan.getScanId(), sastScan.getBuildId());
			LocalDate localDate = sastScan.getScannedOn().toLocalDate();
			if(existingSastScans.stream().filter(item -> item.getScannedOn().toLocalDate().equals(localDate)).count() > 0) {
				//return "Already parsed this xml file";
				return rootNode;
			}
			sastScan.setName(rootNode.has("app_name") ? rootNode.get("app_name").asText() : null);
			sastScan.setScannedBy(rootNode.has("submitter") ? rootNode.get("submitter").asText() : null);
			sastScan.setTotalFlaws(rootNode.has("total_flaws") ? Long.valueOf(rootNode.get("total_flaws").asText()) : null);
			sastScan.setFlawsNotMitigated(rootNode.has("flaws_not_mitigated") ? Long.valueOf(rootNode.get("flaws_not_mitigated").asText()) : null);
			sastScan.setStaticAnalysisUnitId(rootNode.has("static_analysis_unit_id") ? rootNode.get("static_analysis_unit_id").asText() : null);
			sastScan.setPolicyName(rootNode.has("policy_name") ? rootNode.get("policy_name").asText() : null);
			sastScan.setPolicyComplianceStatus(rootNode.has("policy_compliance_status") ? rootNode.get("policy_compliance_status").asText() : null);
			sastScan.setAnalysisRating(rootNode.has("static-analysis") && rootNode.get("static-analysis").has("rating") ? rootNode.get("static-analysis").get("rating").asText() : null );
			sastScan.setToolAppId(rootNode.has("app_id") ? rootNode.get("app_id").asText() : null);
			sastScan.setToolAccountId(rootNode.has("account_id") ? rootNode.get("account_id").asText() : null);
			sastScan.setScore(rootNode.has("static-analysis") && rootNode.get("static-analysis").has("score") ? rootNode.get("static-analysis").get("score").asText() : null);
			sastScan.setToolId(tool); // setting tool
			sastScan.setApplication(application);
//			Need to set application and bit bucket
			sastScan = sastScanRepository.save(sastScan);
			
			List<SASTVulnerabilities> listOfSASTVulnerabilities = new ArrayList<>();
			if(rootNode.get("severity").isArray()) {
				ArrayNode arrayNode = (ArrayNode) rootNode.get("severity");
				Iterator<JsonNode> severityList = arrayNode.elements();
				while(severityList.hasNext()) {
			    	JsonNode severity = severityList.next();
			    	List<JsonNode> categoryList = new ArrayList<>();
					if(severity.has("category") && severity.get("category").isArray()){
						ArrayNode array1Node = (ArrayNode) severity.get("category");
						Iterator<JsonNode> category = array1Node.elements();
						category.forEachRemaining(categoryList::add);
			    	} else {
			    		if(severity.has("category")){
			    			categoryList.add(severity.get("category"));
			    		}
			    	}
					for(JsonNode category : categoryList) {
						String recommendations = category.path("recommendations").path("para").path("text").asText();
						if(category.has("cwe")) {
							List<JsonNode> cweList = new ArrayList<>();
							if(category.get("cwe").isArray()) {
								ArrayNode array2Node = (ArrayNode) category.get("cwe");
								Iterator<JsonNode> cwe = array2Node.elements();
								cwe.forEachRemaining(cweList::add);
							} else {
								JsonNode cwe = category.get("cwe");
								cweList.add(cwe);
							}
							for(JsonNode cwe : cweList) {
								String cwename = cwe.has("cwename") ? cwe.get("cwename").asText() : null;
								String cweDescription = cwe.has("description") && cwe.get("description").has("text") && cwe.get("description").get("text").has("text") ? cwe.path("description").path("text").path("text").asText() : null;
								JsonNode flw = cwe.get("staticflaws");
								if(flw.has("flaw")) {
									List<JsonNode> flawsList = new ArrayList<>();
									if(flw.get("flaw").isArray()) {
										ArrayNode array3Node = (ArrayNode) flw.get("flaw");
										Iterator<JsonNode> flaws = array3Node.elements();
										flaws.forEachRemaining(flawsList::add);
									} else {
										JsonNode flaw = flw.get("flaw");
										flawsList.add(flaw);
									}
									for(JsonNode flaw : flawsList) {
										SASTVulnerabilities sastVulnerabilities = new SASTVulnerabilities();
										sastVulnerabilities.setCategoryId(flaw.has("categoryid") ? Long.valueOf(flaw.get("categoryid").asText()) : null);
										sastVulnerabilities.setCategoryName(flaw.has("categoryname") ? flaw.get("categoryname").asText() : null);
										sastVulnerabilities.setDescription(flaw.has("description") ? flaw.get("description").asText() : null);
										sastVulnerabilities.setSeverityLevel(flaw.has("severity") ? flaw.get("severity").asText() : null);
										sastVulnerabilities.setRemediationStatus(flaw.has("remediation_status") ? flaw.get("remediation_status").asText() : null);
										sastVulnerabilities.setRecommendations(recommendations);
										sastVulnerabilities.setDateFirstOccurrence(flaw.has("date_first_occurrence") ? LocalDateTime.parse(flaw.get("date_first_occurrence").asText(), formatter) : null);
										sastVulnerabilities.setCweId(flaw.has("cweid") ? Long.valueOf(flaw.get("cweid").asText()) : null);
										sastVulnerabilities.setCweName(cwename);
										sastVulnerabilities.setCweDescription(cweDescription);
										sastVulnerabilities.setCount(flaw.has("count") ? Long.valueOf(flaw.get("count").asText()) : null);
										sastVulnerabilities.setModule(flaw.has("module") ? flaw.get("module").asText() : null);
										sastVulnerabilities.setIssueId(flaw.has("issueid") ? flaw.get("issueid").asText() : null);
										sastVulnerabilities.setSourceFile(flaw.has("sourcefile") ? flaw.get("sourcefile").asText() : null);
										sastVulnerabilities.setSourceFilePath(flaw.has("sourcefilepath") ? flaw.get("sourcefilepath").asText() : null);
										sastVulnerabilities.setLineNumber(flaw.has("line") ? Long.valueOf(flaw.get("line").asText()) : null);
										sastVulnerabilities.setFunctionPrototype(flaw.has("functionprototype") ? flaw.get("functionprototype").asText() : null);
										sastVulnerabilities.setSastScan(sastScan);
										listOfSASTVulnerabilities.add(sastVulnerabilities);
									}
								}
							}
						}
					}
					
			    }
			}
			sastVulnerabilitiesRepository.saveAll(listOfSASTVulnerabilities);
			SASTOverview sastOverview = new SASTOverview();
			Optional<SASTOverview> optionalSastOverview = sastOverviewRepository.findById(sastScan.getId());
			if(optionalSastOverview.isPresent() ) {
				sastOverview = optionalSastOverview.get();
			}
			if(rootNode.has("flaw-status")) {
				JsonNode flawStatus = rootNode.get("flaw-status");
				sastOverview.setNewFlaws(flawStatus.has("new") ? Long.valueOf(flawStatus.get("new").asText()) : null);
				sastOverview.setFlawsReopened(flawStatus.has("reopen") ? Long.valueOf(flawStatus.get("reopen").asText()) : null);
				sastOverview.setFlawsOpen(flawStatus.has("open") ? Long.valueOf(flawStatus.get("open").asText()) : null);
				sastOverview.setFlawsFixed(flawStatus.has("fixed") ? Long.valueOf(flawStatus.get("fixed").asText()) : null);
				sastOverview.setTotalFlaws(flawStatus.has("total") ? Long.valueOf(flawStatus.get("total").asText()) : null);
				sastOverview.setFlawsNotMitigated(flawStatus.has("not_mitigated") ? Long.valueOf(flawStatus.get("not_mitigated").asText()) : null);
			}
			
			if(rootNode.has("static-analysis") && rootNode.get("static-analysis").has("modules") && rootNode.get("static-analysis").get("modules").has("module")) {
				List<JsonNode> moduleList = new ArrayList<>();
				if(rootNode.get("static-analysis").get("modules").get("module").isArray()){
					ArrayNode array1Node = (ArrayNode) rootNode.get("static-analysis").get("modules").get("module");
					Iterator<JsonNode> module = array1Node.elements();
					module.forEachRemaining(moduleList::add);
		    	} else {
		    		moduleList.add(rootNode.get("static-analysis").get("modules").get("module"));
		    	}
				Long numflawssev0 = 0L;
				Long numflawssev1 = 0L;
				Long numflawssev2 = 0L;
				Long numflawssev3 = 0L;
				Long numflawssev4 = 0L;
				Long numflawssev5 = 0L;
				for(JsonNode module : moduleList) {
					numflawssev0 += (module.has("numflawssev0") ? module.get("numflawssev0").asLong() : 0L);
					numflawssev1 += (module.has("numflawssev1") ? module.get("numflawssev1").asLong() : 0L);
					numflawssev2 += (module.has("numflawssev2") ? module.get("numflawssev2").asLong() : 0L);
					numflawssev3 += (module.has("numflawssev3") ? module.get("numflawssev3").asLong() : 0L);
					numflawssev4 += (module.has("numflawssev4") ? module.get("numflawssev4").asLong() : 0L);
					numflawssev5 += (module.has("numflawssev5") ? module.get("numflawssev5").asLong() : 0L);
				}
				sastOverview.setTotalFlawsSev0(numflawssev0);
				sastOverview.setTotalFlawsSev1(numflawssev1);
				sastOverview.setTotalFlawsSev2(numflawssev2);
				sastOverview.setTotalFlawsSev3(numflawssev3);
				sastOverview.setTotalFlawsSev4(numflawssev4);
				sastOverview.setTotalFlawsSev5(numflawssev5);
				sastOverview.setSastScan(sastScan);
				sastOverviewRepository.save(sastOverview);
			}
			return "Success";
		} catch (Exception e) {
			e.printStackTrace();
        }
		return "Failed";
	}

	@Override
	public Object parcingTheXmlFileForSCA(byte[] inputstream, String fileName) {
		try {
			XmlMapper xmlMapper = new XmlMapper();
			JsonNode rootNode = xmlMapper.readTree(inputstream);
			String databaseUrl = "jdbc:mysql://localhost:3306/xmlextraction0002";
			String databaseUsername = "root";
			String databasePassword = "1719Sures#";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

			if (rootNode != null && !rootNode.isEmpty()) {
				Class.forName("com.mysql.cj.jdbc.Driver");
				try (Connection conn = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword)) {
					System.out.println("Connection successfull");
					if(rootNode.has("software_composition_analysis")) {
						if(rootNode.get("software_composition_analysis").has("vulnerable_components")) {
							if(rootNode.get("software_composition_analysis").get("vulnerable_components").has("component")) {
								JsonNode component = rootNode.get("software_composition_analysis").get("vulnerable_components").get("component");
								List<JsonNode> componentNodes = new ArrayList<>();
								if(component.isArray()) {
									ArrayNode arrayNode = (ArrayNode) component;
									Iterator<JsonNode> components = arrayNode.elements();
									components.forEachRemaining(componentNodes::add);
								} else {
									componentNodes.add(component);
								}
								
								String sastScanId = UUID.randomUUID().toString();
								String insertComponentStatement = "INSERT INTO sca_component(id, scan_id, violated_policy_rules, component_id, file_name, sha1, vulnerabilities, max_cvss_score, version, library_name, library_id, vendor, description, added_date, component_affects_policy_compliance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
								for(JsonNode componentNode : componentNodes) {
									String componentId = UUID.randomUUID().toString();
									try (PreparedStatement preparedStatement = conn.prepareStatement(insertComponentStatement)) {
										preparedStatement.setString(1, componentId);
					    				preparedStatement.setString(2, sastScanId);
					    				preparedStatement.setString(3, componentNode.has("violated_policy_rules") ? componentNode.get("violated_policy_rules").asText() : null);
					    				preparedStatement.setString(4, componentNode.has("component_id") ? componentNode.get("component_id").asText() : null);
					    				preparedStatement.setString(5, componentNode.has("file_name") ? componentNode.get("file_name").asText() : null);
					    				preparedStatement.setString(6, componentNode.has("sha1") ? componentNode.get("sha1").asText() : null);
					    				if(componentNode.has("vulnerabilities") && componentNode.get("vulnerabilities").isArray()) {
					    					ArrayNode arrayNode = (ArrayNode) componentNode.get("vulnerabilities");
					    					JsonNode vulnerabilitiesCountText = arrayNode.get(0);
										    Long vulnerabilitiesCount = !vulnerabilitiesCountText.asText().equalsIgnoreCase("") ? Long.valueOf(vulnerabilitiesCountText.asText()) : null;
										    preparedStatement.setLong(7, vulnerabilitiesCount);
										    if(vulnerabilitiesCount != null && vulnerabilitiesCount != 0L) {
										    	JsonNode vulnerabilitiesNode = arrayNode.get(1);
										    	List<JsonNode> vulnerabilities = new ArrayList<>();
												if(vulnerabilitiesNode.get("vulnerability").isArray()) {
													ArrayNode arrayNodeOfVulnerabilities = (ArrayNode) vulnerabilitiesNode.get("vulnerability");
													Iterator<JsonNode> vulnerabilitiesIterator = arrayNodeOfVulnerabilities.elements();
													vulnerabilitiesIterator.forEachRemaining(vulnerabilities::add);
												} else {
													vulnerabilities.add(vulnerabilitiesNode.get("vulnerability"));
												}
												for(JsonNode vulnerability : vulnerabilities) {
													String vulnerabilitiesInsertStatement = "INSERT INTO vulnerabilities (id, mitigations, cve_id, cvss_score, severity_level, date_first_occurrence, cwe_id, cve_summary, severity_desc, mitigation, vulnerability_affects_policy_compliance, scan_type ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
													String vulnerabilitiesUUID = UUID.randomUUID().toString();
													try (PreparedStatement preparedStatementForVulnerabilities = conn.prepareStatement(vulnerabilitiesInsertStatement)) {
														preparedStatementForVulnerabilities.setString(1, vulnerabilitiesUUID);
														preparedStatementForVulnerabilities.setString(2, vulnerability.has("mitigations") ? vulnerability.get("mitigations").asText() : null);
														preparedStatementForVulnerabilities.setString(3, vulnerability.has("cve_id") ? vulnerability.get("cve_id").asText() : null);
														preparedStatementForVulnerabilities.setDouble(4, vulnerability.has("cvss_score") ? Double.valueOf(vulnerability.get("cvss_score").asText()) : null);
														preparedStatementForVulnerabilities.setDouble(5, vulnerability.has("severity") ? Double.valueOf(vulnerability.get("severity").asText()) : null);
														preparedStatementForVulnerabilities.setTimestamp(6, vulnerability.has("first_found_date") ? Timestamp.valueOf(LocalDateTime.parse(vulnerability.get("first_found_date").asText(), formatter)) : null);
														preparedStatementForVulnerabilities.setString(7, vulnerability.has("cwe_id") ? vulnerability.get("cwe_id").asText() : null);
														preparedStatementForVulnerabilities.setString(8, vulnerability.has("cve_summary") ? vulnerability.get("cve_summary").asText() : null);
														preparedStatementForVulnerabilities.setString(9, vulnerability.has("severity_desc") ? vulnerability.get("severity_desc").asText() : null);
														preparedStatementForVulnerabilities.setBoolean(10, vulnerability.has("mitigation") ? Boolean.valueOf(vulnerability.get("mitigation").asText()) : null);
														preparedStatementForVulnerabilities.setBoolean(11, vulnerability.has("vulnerability_affects_policy_compliance") ? Boolean.valueOf(vulnerability.get("vulnerability_affects_policy_compliance").asText()) : null);
														preparedStatementForVulnerabilities.setString(12, "SCA");
														preparedStatementForVulnerabilities.executeUpdate();
													} catch(SQLException e) {
									    				e.printStackTrace();
									    				throw new RuntimeException(e);
									    			}
													String insertSCAVulnerabilitiesMappingStatement = "INSERT INTO sca_vulnerabilities_mapping(id, vulnerabilities_id, sca_component_id) VALUES (?, ?, ?)";
													try (PreparedStatement preparedStatementForSCAVulnerabilitiesMapping = conn.prepareStatement(insertSCAVulnerabilitiesMappingStatement)) {
														preparedStatementForSCAVulnerabilitiesMapping.setString(1, UUID.randomUUID().toString());
														preparedStatementForSCAVulnerabilitiesMapping.setString(2, vulnerabilitiesUUID);
														preparedStatementForSCAVulnerabilitiesMapping.setString(3, componentId);
														preparedStatementForSCAVulnerabilitiesMapping.executeUpdate();
													} catch (Exception e) {
									    				e.printStackTrace();
									    				throw new RuntimeException(e);
									    			}
												}
										    }
					    				}
					    				Double maxCvssScore = componentNode.has("max_cvss_score") && !componentNode.get("max_cvss_score").asText().equalsIgnoreCase("") ? Double.valueOf(componentNode.get("max_cvss_score").asText()) : 0D;
					    				preparedStatement.setDouble(8, maxCvssScore);
					    				preparedStatement.setString(9, componentNode.has("version") ? componentNode.get("version").asText() : null);
					    				preparedStatement.setString(10, componentNode.has("library") ? componentNode.get("library").asText() : null);
					    				preparedStatement.setString(11, componentNode.has("library_id") ? componentNode.get("library_id").asText() : null);
					    				preparedStatement.setString(12, componentNode.has("vendor") ? componentNode.get("vendor").asText() : null);
					    				preparedStatement.setString(13, componentNode.has("description") ? componentNode.get("description").asText() : null);
					    				preparedStatement.setTimestamp(14, componentNode.has("added_date") ? Timestamp.valueOf(LocalDateTime.parse(componentNode.get("added_date").asText(), formatter)) : null);
					    				preparedStatement.setBoolean(15, componentNode.has("component_affects_policy_compliance") && !componentNode.get("component_affects_policy_compliance").asText().equalsIgnoreCase("") ? Boolean.valueOf(componentNode.get("component_affects_policy_compliance").asText()) : null);
					    				
					    				if(componentNode.has("file_paths")) {
					    					List<JsonNode> fileNodes = new ArrayList<>();
											if(componentNode.get("file_paths").get("file_path").isArray()) {
												ArrayNode arrayNode = (ArrayNode) componentNode.get("file_paths").get("file_path");
												Iterator<JsonNode> fileNode = arrayNode.elements();
												fileNode.forEachRemaining(fileNodes::add);
											} else {
												fileNodes.add(componentNode.get("file_paths").get("file_path"));
											}
											for(JsonNode fileNode : fileNodes) {
												String insertFilePathStatement = "INSERT INTO sca_component_files(id, file_path, sca_component_id) VALUES (?, ?, ?)";
												try (PreparedStatement preparedStatementForFilePath = conn.prepareStatement(insertFilePathStatement)) {
													String fileUUID = UUID.randomUUID().toString();
													preparedStatementForFilePath.setString(1, fileUUID);
													preparedStatementForFilePath.setString(2, fileNode.has("value") ? fileNode.get("value").asText() : null);
													preparedStatementForFilePath.setString(3, componentId);
													preparedStatementForFilePath.executeUpdate();
												} catch (Exception e) {
								    				e.printStackTrace();
								    				throw new RuntimeException(e);
								    			}
											}
					    				}
					    				
					    				if(componentNode.has("licenses")) {
					    					List<JsonNode> licensesNodes = new ArrayList<>();
											if(componentNode.get("licenses").get("license").isArray()) {
												ArrayNode arrayNode = (ArrayNode) componentNode.get("licenses").get("license");
												Iterator<JsonNode> licenses = arrayNode.elements();
												licenses.forEachRemaining(licensesNodes::add);
											} else {
												licensesNodes.add(componentNode.get("licenses").get("license"));
											}
											for(JsonNode licensesNode : licensesNodes) {
												String insertLicenesStatement = "INSERT INTO sca_component_licenses(id, mitigations, name, spdx_id, url, risk_rating, mitigation, affects_policy_compliance, sca_component_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
												try (PreparedStatement preparedStatementForLicenses = conn.prepareStatement(insertLicenesStatement)) {
													String licensesId = UUID.randomUUID().toString();
													preparedStatementForLicenses.setString(1, licensesId);
													preparedStatementForLicenses.setDouble(2, licensesNode.has("mitigations") ? (licensesNode.get("mitigations").asText().equalsIgnoreCase("") ? 0D : Double.valueOf(licensesNode.get("mitigations").asText())) : 0D);
													preparedStatementForLicenses.setString(3, licensesNode.has("name") ? licensesNode.get("name").asText() : null);
													preparedStatementForLicenses.setString(4, licensesNode.has("spdx_id") ? licensesNode.get("spdx_id").asText() : null);
													preparedStatementForLicenses.setString(5, licensesNode.has("license_url") ? licensesNode.get("license_url").asText() : null);
													preparedStatementForLicenses.setString(6, licensesNode.has("risk_rating") ? licensesNode.get("risk_rating").asText() : null);
													preparedStatementForLicenses.setBoolean(7, licensesNode.has("mitigation") ? Boolean.valueOf(licensesNode.get("mitigation").asText()) : null);
													preparedStatementForLicenses.setBoolean(8, licensesNode.has("license_affects_policy_compliance") ? Boolean.valueOf(licensesNode.get("license_affects_policy_compliance").asText()) : null);
													preparedStatementForLicenses.setString(9, componentId);
													preparedStatementForLicenses.executeUpdate();
												} catch (Exception e) {
								    				e.printStackTrace();
								    				throw new RuntimeException(e);
								    			}
											}
					    				}
										preparedStatement.executeUpdate();
					    			} catch (Exception e) {
					    				e.printStackTrace();
					    				throw new RuntimeException(e);
					    			}
								 }
								return componentNodes;
							}
						}
					}
					return null;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
