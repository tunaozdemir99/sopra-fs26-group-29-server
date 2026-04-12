package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.rest.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.BucketItem;
import ch.uzh.ifi.hase.soprafs26.entity.Task;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

	DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

	@Mapping(source = "username", target = "username")
	@Mapping(source = "password", target = "password")
	User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "status", target = "status")
	UserGetDTO convertEntityToUserGetDTO(User user);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "token", target = "token") 
	UserLoginDTO convertEntityToUserLoginDTO(User user);

    // trip mappings
    @Mapping(source = "title", target = "title")
    @Mapping(source = "location", target = "location")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    Trip convertTripPostDTOtoEntity(TripPostDTO tripPostDTO);

    @Mapping(source = "tripId", target = "tripId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "location", target = "location")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "inviteUrl", target = "inviteUrl")
    @Mapping(source = "admin.username", target = "adminUsername")
    TripGetDTO convertEntityToTripGetDTO(Trip trip);

    // bucket mappings
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "location", target = "location")
    BucketItem convertBucketItemPostDTOtoEntity(BucketItemPostDTO bucketItemPostDTO);

    @Mapping(source = "bucketItemId", target = "bucketItemId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "location", target = "location")
    @Mapping(source = "addedBy.username", target = "addedBy")
    @Mapping(source = "voteScore", target = "voteScore")
    BucketItemGetDTO convertEntityToBucketItemGetDTO(BucketItem bucketItem);

    @Mapping(source = "taskId", target = "taskId")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "assignee", target = "assignee")
    TaskGetDTO convertEntityToTaskGetDTO(Task task);
}
