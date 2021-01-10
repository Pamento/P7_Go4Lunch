package com.pawel.p7_go4lunch.dataServices.repositorys;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pawel.p7_go4lunch.model.FavoritesRestaurants;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.model.User;
import com.pawel.p7_go4lunch.utils.Const;

public class FirebaseUserRepository {

    private static volatile FirebaseUserRepository instance;
    private final CollectionReference userRepository;

    public FirebaseUserRepository() {
        this.userRepository = getUsersCollection();
    }

    public static FirebaseUserRepository getInstance() {
        if (instance == null) instance = new FirebaseUserRepository();
        return instance;
    }

    // ..................................................................... COLLECTION REFERENCE
    public CollectionReference getUsersCollection() {
        return FirebaseFirestore.getInstance().collection(Const.COLLECTION_USERS);
    }

    // ................................................................................... CREATE
    public Task<Void> createUser(String uid, String name,
                                        String email, String urlImage) {
        User user = new User(uid, name, email, urlImage);
        return userRepository.document(uid).set(user);
    }

    // ................................................................................... GET
    public Task<DocumentSnapshot> getUser(String uid) {
        return userRepository.document(uid).get();
    }

    public Query getAllUsersFromCollection() {
        return userRepository.orderBy(Const.FIREBASE_ADAPTER_QUERY_EMAIL, Query.Direction.DESCENDING);
    }

    public Query getSelectedUsersFromCollection(String restaurantName) {
        return userRepository.whereNotEqualTo("userRestaurant", null)
                .whereEqualTo("name", restaurantName);
                //.whereArrayContains("name", restaurantName);
                //.orderBy(Const.FIREBASE_ADAPTER_QUERY_RATING, Query.Direction.DESCENDING);
    }

    // ................................................................................... UPDATE
    public Task<Void> updateUserRestaurant(String uid, Restaurant chosenRestaurant) {
        return userRepository.document(uid).update("chosenRestaurant", chosenRestaurant);
    }

    public Task<Void> updateUserFavoritesRestaurant(String uid, FavoritesRestaurants favoritesRestaurants) {
        return userRepository.document(uid).update("favoritesRestaurants", favoritesRestaurants);
    }

    // ................................................................................... DELETE
    public Task<Void> deleteUser(String uid) {
        return userRepository.document(uid).delete();
    }
}
