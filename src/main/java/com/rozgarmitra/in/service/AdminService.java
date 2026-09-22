package com.rozgarmitra.in.service;

import com.rozgarmitra.in.DTOs.Request.ServiceCategoryRequestDto;
import com.rozgarmitra.in.DTOs.Response.UserResponseDto;
import com.rozgarmitra.in.DTOs.Response.AdminStatsDto;
import com.rozgarmitra.in.DTOs.Response.AdminTaskDto;
import com.rozgarmitra.in.Entity.ServiceCategory;
import com.rozgarmitra.in.Entity.User;
import com.rozgarmitra.in.Enum.Role;
import com.rozgarmitra.in.repository.ServiceCategoryRepository;
import com.rozgarmitra.in.repository.UserRepository;
import com.rozgarmitra.in.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Wrapper;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Automatically injects final fields (Best Practice)
public class AdminService {

    private final UserRepository userRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public AdminStatsDto getStats() {
        List<User> customers = userRepository.findByRole(Role.ROLE_CUSTOMER);
        List<User> labours = userRepository.findByRole(Role.ROLE_LABOUR);
        List<com.rozgarmitra.in.Entity.Task> tasks = taskRepository.findAll();

        AdminStatsDto stats = new AdminStatsDto();
        stats.setTotalCustomers(customers.size());
        stats.setTotalLabours(labours.size());
        stats.setActiveLabours(labours.stream().filter(user -> Boolean.TRUE.equals(user.getIsOnline())).count());
        stats.setTotalTasks(tasks.size());
        stats.setPlatformRevenue(tasks.stream()
                .filter(task -> task.getStatus() == com.rozgarmitra.in.Enum.TaskStatus.COMPLETED)
                .map(task -> task.getFinalAmount() != null ? task.getFinalAmount() : task.getEstimatedPrice())
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum());
        return stats;
    }

    @Transactional(readOnly = true)
    public List<AdminTaskDto> getTasks(String search, int page, int size) {
        String query = search == null ? "" : search.trim().toLowerCase();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        var tasks = (query.isBlank()
                ? taskRepository.findAllByOrderByCreatedAtDesc(pageable)
                : taskRepository.searchTasks(query, pageable)).getContent();
        return tasks.stream()
                .filter(task -> query.isBlank()
                        || task.getServiceCategory().getName().toLowerCase().contains(query)
                        || task.getCustomer().getName().toLowerCase().contains(query)
                        || (task.getLabour() != null && task.getLabour().getName().toLowerCase().contains(query))
                        || task.getStatus().name().toLowerCase().contains(query))
                .map(this::mapToAdminTask)
                .toList();
    }

    private AdminTaskDto mapToAdminTask(com.rozgarmitra.in.Entity.Task task) {
        AdminTaskDto dto = new AdminTaskDto();
        dto.setId(task.getId());
        dto.setServiceId(task.getServiceCategory().getId());
        dto.setServiceName(task.getServiceCategory().getName());
        dto.setCustomerName(task.getCustomer().getName());
        dto.setCustomerUsername(task.getCustomer().getUsername());
        dto.setCustomerPhone(task.getCustomer().getPhone_number());
        if (task.getLabour() != null) {
            dto.setLabourName(task.getLabour().getName());
            dto.setLabourUsername(task.getLabour().getUsername());
            dto.setLabourPhone(task.getLabour().getPhone_number());
            if (task.getLabour().getCurrentLocation() != null) {
                dto.setLabourLatitude(task.getLabour().getCurrentLocation().getLatitude());
                dto.setLabourLongitude(task.getLabour().getCurrentLocation().getLongitude());
            }
        }
        dto.setStatus(task.getStatus().name());
        dto.setAddressText(task.getAddressText());
        dto.setEstimatedPrice(task.getEstimatedPrice());
        if (task.getTaskLocation() != null) {
            dto.setCustomerLatitude(task.getTaskLocation().getLatitude());
            dto.setCustomerLongitude(task.getTaskLocation().getLongitude());
        }
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setStartOtpGeneratedAt(task.getStartOtpGeneratedAt());
        dto.setStartOtpVerifiedAt(task.getStartOtpVerifiedAt());
        dto.setCompletionOtpGeneratedAt(task.getCompletionOtpGeneratedAt());
        dto.setCompletionOtpVerifiedAt(task.getCompletionOtpVerifiedAt());
        dto.setStartedAt(task.getStartedAt());
        dto.setCompletedAt(task.getCompletedAt());
        dto.setDurationMinutes(task.getDurationMinutes());
        dto.setFinalAmount(task.getFinalAmount());
        dto.setPaymentStatus(task.getPaymentStatus());
        dto.setPaidAt(task.getPaidAt());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsersByRole(Role role) {
        List<User> users = userRepository.findByRole(role);
        return users.stream().map(this::mapToUserResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public List<ServiceCategory> getAllServices() {
        return serviceCategoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ServiceCategory> searchServices(String search) {
        if (search == null || search.isBlank()) {
            return getAllServices();
        }
        String query = search.trim();
        return serviceCategoryRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }

    @Transactional
    public ServiceCategory addService(ServiceCategoryRequestDto dto) {
        if (serviceCategoryRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Service with this name already exists!");
        }

        ServiceCategory service = ServiceCategory.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .pricePerHour(dto.getPricePerHour())
                .build();

        return serviceCategoryRepository.save(service);
    }

    @Transactional
    public ServiceCategory updateService(Long id, ServiceCategoryRequestDto dto) {
        ServiceCategory existingService = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found!"));

        existingService.setName(dto.getName());
        existingService.setDescription(dto.getDescription());
        existingService.setPricePerHour(dto.getPricePerHour());

        return serviceCategoryRepository.save(existingService);
    }

    @Transactional
    public void deleteService(Long id) {
        if (!serviceCategoryRepository.existsById(id)) {
            throw new RuntimeException("Service not found!");
        }
        serviceCategoryRepository.deleteById(id);
    }

    private UserResponseDto mapToUserResponse(User user) {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setName(user.getName());
        responseDto.setUsername(user.getUsername());
        responseDto.setPhone_number(user.getPhone_number());
        responseDto.setEmail(user.getEmail());
        responseDto.setRole(user.getRole());
        responseDto.setDob(user.getDob());
        responseDto.setAddress(user.getAddress());
        responseDto.setId(String.valueOf(user.getId()));
        return responseDto;
    }
}