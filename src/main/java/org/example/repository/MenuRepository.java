package org.example.repository;

import org.example.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByRoleId(Long roleId);
    boolean existsByRoleId(Long roleId);

    @Query("select m from Menu m where m.role is null or m.role.id = :roleId order by m.id")
    List<Menu> findVisibleForRole(@Param("roleId") Long roleId);
}


//Spring Data JPA baca nama methodnya dan otomatis bikin query yang sesuai, selama namanya ikut konvensi (findBy + nama field).

