/*
 * Copyright 2020 Prathab Murugan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myhome.services.springdatajpa;

import com.myhome.controllers.dto.CommunityDto;
import com.myhome.controllers.dto.mapper.CommunityMapper;
import com.myhome.domain.Community;
import com.myhome.domain.CommunityAdmin;
import com.myhome.domain.CommunityHouse;
import com.myhome.repositories.CommunityAdminRepository;
import com.myhome.repositories.CommunityHouseRepository;
import com.myhome.repositories.CommunityRepository;
import com.myhome.services.CommunityService;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CommunitySDJpaService implements CommunityService {
  private final CommunityRepository communityRepository;
  private final CommunityAdminRepository communityAdminRepository;
  private final CommunityMapper communityMapper;
  private final CommunityHouseRepository communityHouseRepository;

  public CommunitySDJpaService(
      CommunityRepository communityRepository,
      CommunityAdminRepository communityAdminRepository,
      CommunityMapper communityMapper,
      CommunityHouseRepository communityHouseRepository) {
    this.communityRepository = communityRepository;
    this.communityAdminRepository = communityAdminRepository;
    this.communityMapper = communityMapper;
    this.communityHouseRepository = communityHouseRepository;
  }

  @Override public Community createCommunity(CommunityDto communityDto) {
    communityDto.setCommunityId(generateUniqueId());
    var community = communityMapper.communityDtoToCommunity(communityDto);
    var savedCommunity = communityRepository.save(community);
    log.trace("saved community with id[{}] to repository", savedCommunity.getId());
    return savedCommunity;
  }

  @Override public Set<Community> listAll() {
    var communityListSet = new HashSet<Community>();
    communityRepository.findAll().forEach(communityListSet::add);
    return communityListSet;
  }

  @Override public Community getCommunityDetailsById(String communityId) {
    return communityRepository.findByCommunityId(communityId);
  }

  @Override public Community addAdminsToCommunity(String communityId, Set<String> admins) {
    var community = communityRepository.findByCommunityId(communityId);

    var savedAdminSet = new HashSet<CommunityAdmin>();
    admins.forEach(s -> {
      var admin = new CommunityAdmin();
      admin.setAdminId(s);
      admin.getCommunities().add(community);
      savedAdminSet.add(communityAdminRepository.save(admin));
    });

    community.getAdmins().addAll(savedAdminSet);
    return communityRepository.save(community);
  }

  // Returns houseId which was added to the community
  @Override
  public Set<String> addHousesToCommunity(String communityId, Set<CommunityHouse> houses) {
    houses.forEach(communityHouse -> communityHouse.setHouseId(generateUniqueId()));
    var community = communityRepository.findByCommunityId(communityId);
    houses.forEach(communityHouse -> communityHouse.setCommunity(community));
    var savedHouses = new HashSet<CommunityHouse>();
    communityHouseRepository.saveAll(houses).forEach(savedHouses::add);
    community.getHouses().addAll(savedHouses);
    communityRepository.save(community);

    var houseIds = new HashSet<String>(savedHouses.size());
    savedHouses.forEach(communityHouse -> houseIds.add(communityHouse.getHouseId()));
    return houseIds;
  }

  private String generateUniqueId() {
    return UUID.randomUUID().toString();
  }
}
