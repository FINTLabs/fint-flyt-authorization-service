package no.fintlabs.flyt.authorization.adminuser;

import no.fintlabs.flyt.authorization.user.UserPermission;
import no.fintlabs.flyt.authorization.user.UserPermissionDto;
import no.fintlabs.flyt.authorization.user.UserPermissionRepository;
import no.fintlabs.flyt.azure.AzureUserCache;
import no.fintlabs.flyt.azure.AzureUserCacheRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/authorization/adminuser")
public class AdminUserController {

    private final UserPermissionRepository userPermissionRepository;
    private final AzureUserCacheRepository azureUserCacheRepository;

    public AdminUserController(
            UserPermissionRepository userPermissionRepository,
            AzureUserCacheRepository azureUserCacheRepository
    ) {
        this.userPermissionRepository = userPermissionRepository;
        this.azureUserCacheRepository = azureUserCacheRepository;
    }


    @GetMapping("check-is-admin")
    public Mono<ResponseEntity<AdminUser>> checkAdminUser(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return isAdmin(authenticationMono)
                .map(isAdmin -> ResponseEntity.ok(AdminUser.builder().admin(isAdmin).build()));
    }

    @GetMapping("userpermissions")
    public Mono<ResponseEntity<List<UserPermissionDto>>> getUserPermissions(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return isAdmin(authenticationMono)
                .flatMap(isAdmin -> {
                    if (isAdmin) {
                        return Mono.fromCallable(userPermissionRepository::findAll)
                                .subscribeOn(Schedulers.boundedElastic())
                                .map(userPermissions -> {
                                    List<UserPermissionDto> userPermissionDtos = new java.util.ArrayList<>(List.of());
                                    userPermissions.forEach(userPermission -> userPermissionDtos.add(buildUserPermissionDto(userPermission)));
                                    userPermissionDtos.sort(Comparator.comparing(UserPermissionDto::getEmail, Comparator.nullsLast(String::compareTo)));
                                    return ResponseEntity.ok().body(userPermissionDtos);
                                });
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }
                });
    }

    @PostMapping("userpermissions")
    public Mono<ResponseEntity<List<UserPermissionDto>>> setUserPermissions(
            @RequestBody List<UserPermissionDto> userPermissionDtos,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        return isAdmin(authenticationMono)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(isAdmin -> {
                    if (isAdmin) {

                        return Flux.fromIterable(userPermissionDtos)
                                .flatMap(userPermissionDto -> Mono.fromCallable(() -> {
                                    Optional<UserPermission> userPermissionOptional = userPermissionRepository
                                            .findByObjectIdentifier(userPermissionDto.getObjectIdentifier());
                                    if (userPermissionOptional.isPresent()) {
                                        UserPermission userPermission = userPermissionOptional.get();
                                        userPermission.setSourceApplicationIds(userPermissionDto
                                                .getSourceApplicationIds());
                                        userPermissionRepository.save(userPermission);

                                        return buildUserPermissionDto(userPermission);
                                    } else {
                                        UserPermission newUserPermission = UserPermission.builder()
                                                .objectIdentifier(userPermissionDto.getObjectIdentifier())
                                                .sourceApplicationIds(userPermissionDto.getSourceApplicationIds())
                                                .build();
                                        userPermissionRepository.save(newUserPermission);

                                        return buildUserPermissionDto(newUserPermission);
                                    }
                                }).subscribeOn(Schedulers.boundedElastic()))
                                .collectList()
                                .map(userPermissionDtoList -> {
                                    userPermissionDtoList.sort(Comparator.comparing(UserPermissionDto::getEmail, Comparator.nullsLast(String::compareTo)));
                                    return ResponseEntity.ok().body(userPermissionDtoList);
                                });
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }
                });
    }

    private UserPermissionDto buildUserPermissionDto(UserPermission userPermission) {

        AzureUserCache azureUserCache = azureUserCacheRepository.findByObjectIdentifier(userPermission.getObjectIdentifier());

        return UserPermissionDto
                .builder()
                .objectIdentifier(userPermission.getObjectIdentifier())
                .email(azureUserCache != null ? azureUserCache.getEmail() : null)
                .sourceApplicationIds(userPermission.getSourceApplicationIds())
                .build();
    }

    private Mono<Boolean> isAdmin(Mono<Authentication> authenticationMono) {
        return authenticationMono
                .map(authentication -> authentication != null && authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));
    }

}
