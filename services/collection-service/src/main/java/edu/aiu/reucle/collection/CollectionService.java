package edu.aiu.reucle.collection;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/** Application service for FR5-FR6. Persistence and gateways are ports. */
public final class CollectionService {
    public enum RequestStatus { PENDING, ASSIGNED, COLLECTED, SKIPPED, DELIVERED }

    public record Location(String label, double latitude, double longitude) {
        public Location { Objects.requireNonNull(label); }
    }

    public record BinCandidate(String binId, double fillLevel, Location location, Instant observedAt) {
        public BinCandidate {
            Objects.requireNonNull(binId); Objects.requireNonNull(location); Objects.requireNonNull(observedAt);
            if (fillLevel < 0 || fillLevel > 100) throw new IllegalArgumentException("fillLevel must be 0..100");
        }
    }

    public static final class CollectionRequest {
        private final String requestId;
        private final String binId;
        private final double fillLevel;
        private final Location location;
        private final Instant createdAt;
        private RequestStatus status;
        private String vehicleId;
        private String driverId;
        private String skipReason;

        public CollectionRequest(String requestId, String binId, double fillLevel, Location location,
                                 Instant createdAt, RequestStatus status) {
            this.requestId = Objects.requireNonNull(requestId); this.binId = Objects.requireNonNull(binId);
            this.fillLevel = fillLevel; this.location = Objects.requireNonNull(location);
            this.createdAt = Objects.requireNonNull(createdAt); this.status = Objects.requireNonNull(status);
        }
        public void assign(String vehicleId, String driverId) {
            transitionTo(RequestStatus.ASSIGNED); this.vehicleId = Objects.requireNonNull(vehicleId); this.driverId = Objects.requireNonNull(driverId);
        }
        public void markCollected() { transitionTo(RequestStatus.COLLECTED); }
        public void markSkipped(String reason) { if (reason == null || reason.isBlank()) throw new IllegalArgumentException("skip reason is required"); transitionTo(RequestStatus.SKIPPED); skipReason = reason; }
        public void markDelivered() { transitionTo(RequestStatus.DELIVERED); }
        public void transitionTo(RequestStatus next) {
            boolean allowed = (status == RequestStatus.PENDING && next == RequestStatus.ASSIGNED)
                    || (status == RequestStatus.ASSIGNED && (next == RequestStatus.COLLECTED || next == RequestStatus.SKIPPED))
                    || (status == RequestStatus.COLLECTED && next == RequestStatus.DELIVERED);
            if (!allowed) throw new InvalidCollectionRequestTransitionException(status, next);
            status = next;
        }
        public String requestId() { return requestId; }
        public String binId() { return binId; }
        public double fillLevel() { return fillLevel; }
        public Location location() { return location; }
        public Instant createdAt() { return createdAt; }
        public RequestStatus status() { return status; }
        public String vehicleId() { return vehicleId; }
        public String driverId() { return driverId; }
        public String skipReason() { return skipReason; }
    }

    public record RoutePlanCommand(String vehicleId, String driverId, LocalDate date, Location depot) {
        public RoutePlanCommand { Objects.requireNonNull(vehicleId); Objects.requireNonNull(driverId); Objects.requireNonNull(date); Objects.requireNonNull(depot); }
    }
    public record RouteStop(int orderIndex, String requestId, String binId, Location location, double fromPreviousKm) { }
    public record Route(String vehicleId, LocalDate date, List<RouteStop> stops, double totalDistanceKm) {
        public Route { stops = List.copyOf(stops); }
    }
    public record CreateRequestsResult(int createdCount, int duplicateCount, List<String> requestIds) {
        public CreateRequestsResult { requestIds = List.copyOf(requestIds); }
    }

    public interface IBin { List<BinCandidate> getBinsNeedingCollection(); }
    public interface IMapsAPI { double[][] getDistanceMatrix(List<Location> locations); }
    public interface ICollectionRepository {
        Optional<CollectionRequest> findOpenByBinId(String binId);
        List<CollectionRequest> findPending();
        void save(CollectionRequest request);
    }

    public static class BinGatewayException extends RuntimeException {
        public BinGatewayException(String message, Throwable cause) { super(message, cause); }
    }
    public static class MapsGatewayException extends RuntimeException {
        public MapsGatewayException(String message, Throwable cause) { super(message, cause); }
    }
    public static class InvalidCollectionRequestTransitionException extends RuntimeException {
        public InvalidCollectionRequestTransitionException(RequestStatus from, RequestStatus to) { super("Invalid collection request transition: " + from + " -> " + to); }
    }

    private final IBin bin;
    private final IMapsAPI maps;
    private final ICollectionRepository repository;
    private final Supplier<String> requestIdSupplier;
    private final Supplier<Instant> clock;

    public CollectionService(IBin bin, IMapsAPI maps, ICollectionRepository repository,
                             Supplier<String> requestIdSupplier, Supplier<Instant> clock) {
        this.bin = Objects.requireNonNull(bin); this.maps = Objects.requireNonNull(maps);
        this.repository = Objects.requireNonNull(repository); this.requestIdSupplier = Objects.requireNonNull(requestIdSupplier);
        this.clock = Objects.requireNonNull(clock);
    }

    public CreateRequestsResult createAutomaticRequests() {
        final List<BinCandidate> candidates;
        try { candidates = bin.getBinsNeedingCollection(); }
        catch (RuntimeException e) { throw new BinGatewayException("Could not read collection candidates", e); }
        int created = 0, duplicates = 0; List<String> ids = new ArrayList<>();
        for (BinCandidate candidate : candidates) {
            if (repository.findOpenByBinId(candidate.binId()).isPresent()) { duplicates++; continue; }
            CollectionRequest request = new CollectionRequest(requestIdSupplier.get(), candidate.binId(), candidate.fillLevel(),
                    candidate.location(), clock.get(), RequestStatus.PENDING);
            repository.save(request); ids.add(request.requestId()); created++;
        }
        return new CreateRequestsResult(created, duplicates, ids);
    }

    public Route createOptimizedRoute(RoutePlanCommand command) {
        createAutomaticRequests();
        List<CollectionRequest> pending = repository.findPending();
        if (pending.isEmpty()) return new Route(command.vehicleId(), command.date(), List.of(), 0.0);
        pending = new ArrayList<>(pending);
        List<Location> locations = new ArrayList<>(); locations.add(command.depot());
        pending.forEach(r -> locations.add(r.location()));
        final double[][] distances;
        try { distances = maps.getDistanceMatrix(List.copyOf(locations)); }
        catch (RuntimeException e) { throw new MapsGatewayException("Could not obtain road distances", e); }
        validateMatrix(distances, locations.size());

        List<RouteStop> stops = new ArrayList<>(); Set<Integer> visited = new HashSet<>();
        int current = 0; double total = 0;
        for (int order = 1; order <= pending.size(); order++) {
            int next = -1; double best = Double.POSITIVE_INFINITY;
            for (int matrixIndex = 1; matrixIndex < locations.size(); matrixIndex++) {
                if (!visited.contains(matrixIndex) && distances[current][matrixIndex] < best) {
                    best = distances[current][matrixIndex]; next = matrixIndex;
                }
            }
            int requestIndex = next - 1; CollectionRequest request = pending.get(requestIndex);
            visited.add(next); total += best;
            request.assign(command.vehicleId(), command.driverId()); repository.save(request);
            stops.add(new RouteStop(order, request.requestId(), request.binId(), request.location(), best)); current = next;
        }
        return new Route(command.vehicleId(), command.date(), stops, total);
    }

    private static void validateMatrix(double[][] matrix, int expectedSize) {
        if (matrix == null || matrix.length != expectedSize) throw new MapsGatewayException("Distance matrix has wrong dimensions", null);
        for (double[] row : matrix) if (row == null || row.length != expectedSize) throw new MapsGatewayException("Distance matrix has wrong dimensions", null);
    }
}
