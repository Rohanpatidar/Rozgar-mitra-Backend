package com.rozgarmitra.in.service;

import com.rozgarmitra.in.DTOs.Request.TaskRequestDto;
import com.rozgarmitra.in.DTOs.Response.TaskResponseDto;
import com.rozgarmitra.in.Entity.*;
import com.rozgarmitra.in.DTOs.Response.LabourStatsDto;
import com.rozgarmitra.in.Enum.TaskStatus;
import com.rozgarmitra.in.repository.*;
import com.rozgarmitra.in.Utils.LocationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.security.SecureRandom;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class TaskService {

    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    private static final double MAX_RADIUS_KM = 12.0; // 12 KM limit
    private static final long PENDING_REQUEST_TIMEOUT_MINUTES = 5;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expirePendingRequests() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(PENDING_REQUEST_TIMEOUT_MINUTES);
        List<Task> expiredTasks = taskRepository.findByStatusAndCreatedAtBefore(TaskStatus.PENDING, cutoff);
        expiredTasks.forEach(task -> task.setStatus(TaskStatus.REJECTED));
        if (!expiredTasks.isEmpty()) {
            taskRepository.saveAll(expiredTasks);
        }
    }
    public TaskResponseDto createTask(String username, TaskRequestDto requestDto) {
        expirePendingRequests();
        User customer = userRepository.findByUsername(username);
        if (customer == null) {
            throw new RuntimeException("Customer not found with username: " + username);
        }

        ServiceCategory category = serviceCategoryRepository.findById(requestDto.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service Category not found"));

        if (userRepository.countByRoleAndIsOnlineTrue(com.rozgarmitra.in.Enum.Role.ROLE_LABOUR) == 0) {
            throw new IllegalStateException("No labour is online right now. Please try again later.");
        }

        List<TaskStatus> activeStatuses = List.of(TaskStatus.PENDING, TaskStatus.ACCEPTED, TaskStatus.STARTED);
        if (!taskRepository.findByCustomerAndServiceCategoryAndStatusIn(customer, category, activeStatuses).isEmpty()) {
            throw new IllegalStateException("You already have an active request for this service");
        }

        GeoLocation location = new GeoLocation(requestDto.getLatitude(), requestDto.getLongitude());

        Task task = Task.builder()
                .customer(customer)
                .serviceCategory(category)
                .status(TaskStatus.PENDING)
                .taskLocation(location)
                .addressText(requestDto.getAddressText())
                .estimatedPrice(category.getPricePerHour())
                .build();

        taskRepository.save(task);
        return mapToDto(task);
    }

    @Transactional(readOnly = true)
    public LabourStatsDto getLabourStats(String labourUsername) {
        User labour = userRepository.findByUsername(labourUsername);
        if (labour == null)
            throw new RuntimeException("Labour not found with username: " + labourUsername);
        return new LabourStatsDto(
                taskRepository.countByLabour(labour),
                taskRepository.sumTodayEarnings(labour, LocalDate.now().atStartOfDay()));
    }
    public List<TaskResponseDto> getNearbyPendingTasks(String labourUsername) {
        return getNearbyPendingTasks(labourUsername, null);
    }

    public List<TaskResponseDto> getNearbyPendingTasks(String labourUsername, String search) {
        User labour = userRepository.findByUsername(labourUsername);
        if (labour == null) {
            throw new RuntimeException("Labour not found with username: " + labourUsername);
        }

        if (!Boolean.TRUE.equals(labour.getIsOnline())) {
            throw new IllegalStateException("Go online to receive service requests.");
        }

        if (labour.getCurrentLocation() == null || labour.getCurrentLocation().getLatitude() == null) {
            throw new RuntimeException("Labour location is not updated!");
        }

        List<Task> allPendingTasks = taskRepository.findDistinctByStatusOrderByCreatedAtDesc(TaskStatus.PENDING);
        List<TaskResponseDto> nearbyTasks = new ArrayList<>();

        for (Task task : allPendingTasks) {
            double distance = LocationUtils.calculateDistance(
                    labour.getCurrentLocation().getLatitude(),
                    labour.getCurrentLocation().getLongitude(),
                    task.getTaskLocation().getLatitude(),
                    task.getTaskLocation().getLongitude());
            boolean matchesSearch = search == null || search.isBlank()
                    || task.getServiceCategory().getName().toLowerCase().contains(search.trim().toLowerCase())
                    || task.getServiceCategory().getDescription().toLowerCase().contains(search.trim().toLowerCase());
            if (distance <= MAX_RADIUS_KM && matchesSearch && !task.getRejectedLabours().contains(labour)) {
                nearbyTasks.add(mapToDto(task));
            }
        }
        return nearbyTasks;
    }
    public TaskResponseDto acceptTask(Long taskId, String labourUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
        User labour = userRepository.findByUsername(labourUsername);
        if (labour == null) {
            throw new RuntimeException("Labour not found with username: " + labourUsername);
        }

        if (task.getStatus() != TaskStatus.PENDING) {
            throw new IllegalStateException("Task is already accepted or cancelled");
        }

        List<TaskStatus> activeStatuses = List.of(TaskStatus.ACCEPTED, TaskStatus.STARTED);
        if (!taskRepository.findByLabourAndStatusIn(labour, activeStatuses).isEmpty()) {
            throw new IllegalStateException("Complete your current service before accepting another request");
        }

        task.setLabour(labour);
        task.setStatus(TaskStatus.ACCEPTED);
        taskRepository.save(task);

        return mapToDto(task);
    }

    public TaskResponseDto rejectTask(Long taskId, String labourUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));

        User labour = userRepository.findByUsername(labourUsername);
        if (labour == null) {
            throw new RuntimeException("Labour not found with username: " + labourUsername);
        }
        if (task.getStatus() != TaskStatus.PENDING && task.getStatus() != TaskStatus.ACCEPTED) {
            throw new IllegalStateException("A labour can reject only before the service starts");
        }

        task.getRejectedLabours().add(labour);
        if (task.getLabour() != null && task.getLabour().getId().equals(labour.getId())) {
            task.setLabour(null);
        }
        long totalLabours = userRepository.findByRole(com.rozgarmitra.in.Enum.Role.ROLE_LABOUR).size();
        task.setStatus(task.getRejectedLabours().size() >= totalLabours ? TaskStatus.REJECTED : TaskStatus.PENDING);
        taskRepository.save(task);
        return mapToDto(task);
    }

    @Transactional
    public TaskResponseDto customerRejectTask(Long taskId, String customerUsername) {
        Task task = findTaskForCustomer(taskId, customerUsername);
        if (task.getStatus() != TaskStatus.PENDING && task.getStatus() != TaskStatus.ACCEPTED) {
            throw new IllegalStateException("Customer can reject only before the service starts");
        }
        task.setStatus(TaskStatus.REJECTED);
        task.setLabour(null);
        task.setStartOtp(null);
        task.setCompletionOtp(null);
        taskRepository.save(task);
        return mapToDto(task, null, customerUsername);
    }
    public List<TaskResponseDto> getMyTasks(String labourUsername) {
        return getMyTasks(labourUsername, null);
    }

    public List<TaskResponseDto> getMyTasks(String labourUsername, String search) {
        User labour = userRepository.findByUsername(labourUsername);
        if (labour == null) {
            throw new RuntimeException("Labour not found with username: " + labourUsername);
        }
        Set<Task> taskSet = new LinkedHashSet<>(taskRepository.findByLabourOrderByCreatedAtDesc(labour));
        taskSet.addAll(taskRepository.findByRejectedLaboursContainingOrderByCreatedAtDesc(labour));
        List<Task> tasks = new ArrayList<>(taskSet);
        List<TaskResponseDto> taskDtos = new ArrayList<>();

        for (Task task : tasks) {
            if (search != null && !search.isBlank()) {
                String query = search.trim().toLowerCase();
                boolean matches = task.getServiceCategory().getName().toLowerCase().contains(query)
                        || task.getServiceCategory().getDescription().toLowerCase().contains(query);
                if (!matches)
                    continue;
            }
            boolean rejectedByThisLabour = task.getLabour() == null && task.getRejectedLabours().contains(labour);
            taskDtos.add(mapToDto(task, rejectedByThisLabour ? "REJECTED" : null));
        }
        return taskDtos;
    }
    public List<TaskResponseDto> getCustomerBookings(String customerUsername) {
        User customer = userRepository.findByUsername(customerUsername);
        if (customer == null) {
            throw new RuntimeException("Customer not found with username: " + customerUsername);
        }
        List<Task> bookings = taskRepository.findByCustomerOrderByCreatedAtDesc(customer);
        List<TaskResponseDto> bookingDtos = new ArrayList<>();

        for (Task task : bookings) {
            bookingDtos.add(mapToDto(task, null, customerUsername));
        }
        return bookingDtos;
    }

    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getCustomerBookings(String customerUsername, Pageable pageable) {
        User customer = userRepository.findByUsername(customerUsername);
        if (customer == null) {
            throw new RuntimeException("Customer not found with username: " + customerUsername);
        }
        return taskRepository.findByCustomerOrderByCreatedAtDesc(customer, pageable)
                .map(task -> mapToDto(task, null, customerUsername));
    }
    public TaskResponseDto completeTask(Long taskId, String username) {
        throw new IllegalStateException("Completion requires the customer completion OTP");
    }

    @Transactional
    public TaskResponseDto generateStartOtp(Long taskId, String customerUsername) {
        Task task = findTaskForCustomer(taskId, customerUsername);
        if (task.getStatus() != TaskStatus.ACCEPTED) {
            throw new IllegalStateException("Start OTP can be generated only after a labour accepts the request");
        }
        task.setStartOtp(generateOtp());
        task.setStartOtpGeneratedAt(LocalDateTime.now());
        taskRepository.save(task);
        return mapToDto(task, null, customerUsername);
    }

    @Transactional
    public TaskResponseDto verifyStartOtp(Long taskId, String otp, String labourUsername) {
        Task task = findTaskForLabour(taskId, labourUsername);
        if (task.getStatus() != TaskStatus.ACCEPTED || task.getStartOtp() == null) {
            throw new IllegalStateException("Start OTP is not ready for this service");
        }
        String submittedOtp = normalizeOtp(otp);
        if (submittedOtp == null || !task.getStartOtp().equals(submittedOtp)) {
            throw new IllegalStateException("Invalid start OTP");
        }
        LocalDateTime now = LocalDateTime.now();
        task.setStartOtpVerifiedAt(now);
        task.setStartedAt(now);
        task.setStatus(TaskStatus.STARTED);
        taskRepository.save(task);
        return mapToDto(task);
    }

    @Transactional
    public TaskResponseDto generateCompletionOtp(Long taskId, String customerUsername) {
        Task task = findTaskForCustomer(taskId, customerUsername);
        if (task.getStatus() != TaskStatus.STARTED) {
            throw new IllegalStateException("Completion OTP can be generated only after the service has started");
        }
        task.setCompletionOtp(generateOtp());
        task.setCompletionOtpGeneratedAt(LocalDateTime.now());
        taskRepository.save(task);
        return mapToDto(task, null, customerUsername);
    }

    @Transactional
    public TaskResponseDto verifyCompletionOtp(Long taskId, String otp, String labourUsername) {
        Task task = findTaskForLabour(taskId, labourUsername);
        if (task.getStatus() != TaskStatus.STARTED || task.getCompletionOtp() == null) {
            throw new IllegalStateException("Completion OTP is not ready for this service");
        }
        String submittedOtp = normalizeOtp(otp);
        if (submittedOtp == null || !task.getCompletionOtp().equals(submittedOtp)) {
            throw new IllegalStateException("Invalid completion OTP");
        }
        LocalDateTime now = LocalDateTime.now();
        long minutes = Math.max(1, Duration.between(task.getStartedAt(), now).toMinutes());
        task.setCompletionOtpVerifiedAt(now);
        task.setCompletedAt(now);
        task.setDurationMinutes(minutes);
        task.setFinalAmount(task.getEstimatedPrice() * minutes / 60.0);
        task.setPaymentStatus("PENDING");
        task.setStatus(TaskStatus.COMPLETED);
        taskRepository.save(task);
        return mapToDto(task);
    }

    @Transactional
    public TaskResponseDto payForTask(Long taskId, String customerUsername) {
        Task task = findTaskForCustomer(taskId, customerUsername);
        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new IllegalStateException("Payment is available after service completion");
        }
        if ("PAID".equals(task.getPaymentStatus())) {
            throw new IllegalStateException("This service is already paid");
        }
        task.setPaymentStatus("PAID");
        task.setPaidAt(LocalDateTime.now());
        taskRepository.save(task);
        return mapToDto(task, null, customerUsername);
    }

    private Task findTaskForCustomer(Long taskId, String username) {
        Task task = findTask(taskId);
        if (task.getCustomer() == null || !task.getCustomer().getUsername().equals(username)) {
            throw new RuntimeException("You are not authorized for this service");
        }
        return task;
    }

    private Task findTaskForLabour(Long taskId, String username) {
        Task task = findTask(taskId);
        if (task.getLabour() == null || !task.getLabour().getUsername().equals(username)) {
            throw new RuntimeException("You are not authorized for this service");
        }
        return task;
    }

    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
    }

    private String generateOtp() {
        return String.format("%06d", OTP_RANDOM.nextInt(1_000_000));
    }

    private String normalizeOtp(String otp) {
        if (otp == null)
            return null;
        String normalized = otp.trim();
        return normalized.matches("\\d{6}") ? normalized : null;
    }

    private TaskResponseDto mapToDto(Task task) {
        return mapToDto(task, null, null);
    }

    private TaskResponseDto mapToDto(Task task, String statusOverride) {
        return mapToDto(task, statusOverride, null);
    }

    private TaskResponseDto mapToDto(Task task, String statusOverride, String viewerUsername) {
        TaskResponseDto dto = new TaskResponseDto();
        dto.setId(task.getId());
        dto.setServiceId(task.getServiceCategory().getId());
        dto.setCustomerName(task.getCustomer().getName());
        dto.setCustomerUsername(task.getCustomer().getUsername());
        dto.setCustomerPhone(task.getCustomer().getPhone_number());
        dto.setServiceName(task.getServiceCategory().getName());
        if (task.getLabour() != null) {
            dto.setLabourName(task.getLabour().getName());
            dto.setLabourUsername(task.getLabour().getUsername());
            dto.setLabourPhone(task.getLabour().getPhone_number());
            if (task.getLabour().getCurrentLocation() != null) {
                dto.setLabourLatitude(task.getLabour().getCurrentLocation().getLatitude());
                dto.setLabourLongitude(task.getLabour().getCurrentLocation().getLongitude());
            }
        }
        dto.setAddressText(task.getAddressText());
        dto.setEstimatedPrice(task.getEstimatedPrice());
        dto.setStatus(statusOverride != null ? statusOverride : task.getStatus().name());

        if (task.getTaskLocation() != null) {
            dto.setCustomerLat(task.getTaskLocation().getLatitude());
            dto.setCustomerLng(task.getTaskLocation().getLongitude());
            dto.setLatitude(task.getTaskLocation().getLatitude());
            dto.setLongitude(task.getTaskLocation().getLongitude());
            dto.setCustomerLatitude(task.getTaskLocation().getLatitude());
            dto.setCustomerLongitude(task.getTaskLocation().getLongitude());
        }

        dto.setCreatedAt(task.getCreatedAt());
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
        if (task.getCustomer() != null && task.getCustomer().getUsername().equals(viewerUsername)) {
            dto.setStartOtp(task.getStartOtp());
            dto.setCompletionOtp(task.getCompletionOtp());
        }
        return dto;
    }
}
