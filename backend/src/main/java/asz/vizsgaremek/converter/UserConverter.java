package asz.vizsgaremek.converter;

import asz.vizsgaremek.dto.user.UserListItem;
import asz.vizsgaremek.dto.user.UserRead;
import asz.vizsgaremek.dto.user.UserSave;
import asz.vizsgaremek.model.User;

import java.util.List;
import java.util.ArrayList;

public class UserConverter {


    public static List<UserListItem> convertModelToList(List<User> userModels){
        List<UserListItem> users= new ArrayList<>();
        for (User userModel : userModels) {
            users.add(convertModelToListItem(userModel));
        }
        return users;
    }

    protected static UserListItem convertModelToListItem(User userModel){
        UserListItem userListItem = new UserListItem();
        userListItem.setId(userModel.getId());
        userListItem.setName(userModel.getUserName());
        userListItem.setEmail(userModel.getEmail());
        userListItem.setPhone(userListItem.getPhone());
        return userListItem;
    }

    public static User convertSaveToModel(UserSave userSave){
        User user = new User();
        user.setUserName(userSave.getUserName());
        user.setEmail(userSave.getEmail());
        user.setAge(userSave.getAge());
        user.setPhoneNumber(userSave.getPhoneNumber());
        user.setPassword(userSave.getPassword());
        
        return user;
    }

    public static User convertSaveToModel(int id, UserSave userSave){
        User user = convertSaveToModel(userSave);
        user.setId(id);
        return user;
    }

    public static UserRead convertModelToRead(User createdUser){
        UserRead userRead = new UserRead();
        userRead.setId(createdUser.getId());
        userRead.setAge(createdUser.getAge());
        userRead.setUserName(createdUser.getUserName());
        userRead.setEmail(createdUser.getEmail());
        userRead.setPhoneNumber(createdUser.getPhoneNumber());
        userRead.setPassword(createdUser.getPassword());

        return userRead;
    }

    public static UserSave convertModelToSave(User user) {
        UserSave userSave = new UserSave();
        userSave.setEmail(user.getEmail());
        userSave.setAge(user.getAge());
        userSave.setUserName(user.getUserName());
        userSave.setPassword(user.getPassword());
        userSave.setPhoneNumber(user.getPhoneNumber());
        return userSave;
    }

}
