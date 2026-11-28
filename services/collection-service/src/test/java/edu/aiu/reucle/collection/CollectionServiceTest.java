package edu.aiu.reucle.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static edu.aiu.reucle.collection.CollectionService.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CollectionServiceTest {
    static final Instant NOW = Instant.parse("2026-11-25T08:00:00Z");
    static final Location DEPOT = new Location("Depot",30.0444,31.2357);
    static final Location BIN_8821_LOCATION = new Location("Nasr City A",30.0500,31.2400);
    static final Location BIN_4176_LOCATION = new Location("Nasr City B",30.0700,31.2600);
    static final BinCandidate BIN_8821 = new BinCandidate("BIN-8821",82.5,BIN_8821_LOCATION,NOW);
    static final BinCandidate BIN_4176 = new BinCandidate("BIN-4176",91.0,BIN_4176_LOCATION,NOW);
    static final double[][] ROAD_KM={{0.0,4.8,6.2},{4.8,0.0,3.2},{6.2,3.2,0.0}};

    IBin bin; IMapsAPI maps; ICollectionRepository repository; CollectionService service;
    List<CollectionRequest> saved;

    @BeforeEach void setUp(){
        bin=mock(IBin.class);maps=mock(IMapsAPI.class);repository=mock(ICollectionRepository.class);saved=new ArrayList<>();
        when(repository.findOpenByBinId(any())).thenReturn(Optional.empty());
        when(repository.findPending()).thenAnswer(inv->saved.stream().filter(r->r.status()==RequestStatus.PENDING).toList());
        doAnswer(inv->{CollectionRequest r=inv.getArgument(0);if(!saved.contains(r))saved.add(r);return null;}).when(repository).save(any(CollectionRequest.class));
        AtomicInteger ids=new AtomicInteger();
        service=new CollectionService(bin,maps,repository,()->ids.getAndIncrement()==0?"CR-2026-1182":"CR-2026-1183",()->NOW);
    }

    @Test void createsRequestsAndOptimizesTheScenarioRoute(){
        when(bin.getBinsNeedingCollection()).thenReturn(List.of(BIN_8821,BIN_4176));
        when(maps.getDistanceMatrix(any())).thenReturn(ROAD_KM);
        Route route=service.createOptimizedRoute(new RoutePlanCommand("VH-017","DR-104",LocalDate.parse("2026-11-25"),DEPOT));

        assertEquals(List.of("BIN-8821","BIN-4176"),route.stops().stream().map(RouteStop::binId).toList());
        assertEquals(List.of("CR-2026-1182","CR-2026-1183"),route.stops().stream().map(RouteStop::requestId).toList());
        assertEquals(List.of(4.8,3.2),route.stops().stream().map(RouteStop::fromPreviousKm).toList());
        assertEquals(8.0,route.totalDistanceKm(),0.0001);
        assertTrue(saved.stream().allMatch(r->r.status()==RequestStatus.ASSIGNED));
        assertTrue(saved.stream().allMatch(r->"VH-017".equals(r.vehicleId())&&"DR-104".equals(r.driverId())));
        verify(bin).getBinsNeedingCollection();
        verify(repository).findOpenByBinId("BIN-8821"); verify(repository).findOpenByBinId("BIN-4176");
        verify(maps).getDistanceMatrix(List.of(DEPOT,BIN_8821_LOCATION,BIN_4176_LOCATION));
        verify(repository,times(4)).save(any(CollectionRequest.class));
    }

    @Test void doesNotCreateDuplicateRequestForAnAlreadyOpenBin(){
        CollectionRequest existing=new CollectionRequest("CR-OLD","BIN-8821",82.5,BIN_8821_LOCATION,NOW,RequestStatus.PENDING);
        when(bin.getBinsNeedingCollection()).thenReturn(List.of(BIN_8821));
        when(repository.findOpenByBinId("BIN-8821")).thenReturn(Optional.of(existing));
        CreateRequestsResult result=service.createAutomaticRequests();
        assertEquals(0,result.createdCount());assertEquals(1,result.duplicateCount());
        verify(repository,never()).save(any(CollectionRequest.class));
    }

    @Test void noPendingRequestsReturnsAnEmptyRouteWithoutCallingMaps(){
        when(bin.getBinsNeedingCollection()).thenReturn(List.of());
        Route route=service.createOptimizedRoute(new RoutePlanCommand("VH-017","DR-104",LocalDate.parse("2026-11-25"),DEPOT));
        assertTrue(route.stops().isEmpty());assertEquals(0.0,route.totalDistanceKm());verifyNoInteractions(maps);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> validTransitions(){
        return Stream.of(org.junit.jupiter.params.provider.Arguments.of(RequestStatus.PENDING,RequestStatus.ASSIGNED),
                org.junit.jupiter.params.provider.Arguments.of(RequestStatus.ASSIGNED,RequestStatus.COLLECTED),
                org.junit.jupiter.params.provider.Arguments.of(RequestStatus.ASSIGNED,RequestStatus.SKIPPED),
                org.junit.jupiter.params.provider.Arguments.of(RequestStatus.COLLECTED,RequestStatus.DELIVERED));
    }
    static Stream<org.junit.jupiter.params.provider.Arguments> invalidTransitions(){
        List<org.junit.jupiter.params.provider.Arguments> args=new ArrayList<>();
        for(RequestStatus from:RequestStatus.values())for(RequestStatus to:RequestStatus.values())
            if(!((from==RequestStatus.PENDING&&to==RequestStatus.ASSIGNED)
                    ||(from==RequestStatus.ASSIGNED&&(to==RequestStatus.COLLECTED||to==RequestStatus.SKIPPED))
                    ||(from==RequestStatus.COLLECTED&&to==RequestStatus.DELIVERED)))
                args.add(org.junit.jupiter.params.provider.Arguments.of(from,to));
        return args.stream();
    }
    @ParameterizedTest(name="allows {0} -> {1}") @MethodSource("validTransitions")
    void allowsEveryDocumentedRequestTransition(RequestStatus from,RequestStatus to){
        CollectionRequest r=new CollectionRequest("CR-T","BIN-8821",82.5,BIN_8821_LOCATION,NOW,from);
        assertDoesNotThrow(()->r.transitionTo(to));assertEquals(to,r.status());
    }
    @ParameterizedTest(name="rejects {0} -> {1}") @MethodSource("invalidTransitions")
    void rejectsEveryUndocumentedRequestTransition(RequestStatus from,RequestStatus to){
        CollectionRequest r=new CollectionRequest("CR-T","BIN-8821",82.5,BIN_8821_LOCATION,NOW,from);
        assertThrows(InvalidCollectionRequestTransitionException.class,()->r.transitionTo(to));assertEquals(from,r.status());
    }
}
