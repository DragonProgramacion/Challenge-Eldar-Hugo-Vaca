package com.eldar.api.repository;

import com.eldar.api.entity.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountBalance,String> {

}
