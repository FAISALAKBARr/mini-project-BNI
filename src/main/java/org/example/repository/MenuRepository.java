package org.example.repository;

import org.example.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByRoleId(Long roleId);
}
//Spring Data JPA baca nama methodnya dan otomatis bikin query yang sesuai, selama namanya ikut konvensi (findBy + nama field).
