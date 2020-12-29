package com.pawel.p7_go4lunch.dataServices.repositorys;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pawel.p7_go4lunch.model.ChosenRestaurants;
import com.pawel.p7_go4lunch.model.Restaurant;
import com.pawel.p7_go4lunch.utils.Const;

public class FirebaseChosenRestaurants {

    private static volatile FirebaseChosenRestaurants instance;
    private final CollectionReference chosenRestaurantsListCollection;
    public FirebaseChosenRestaurants() {
        this.chosenRestaurantsListCollection = getChosenRestaurantsCollection();
    }

    public static FirebaseChosenRestaurants getInstance() {
        if (instance == null) instance = new FirebaseChosenRestaurants();
        return instance;
    }

    // ..................................................................... COLLECTION REFERENCE
    public CollectionReference getChosenRestaurantsCollection() {
        return FirebaseFirestore.getInstance().collection(Const.COLLECTION_CHOSEN_RESTAURANTS);
    }

    // ................................................................................... CREATE
    public Task<Void> createChosenRestaurant(String placeId, String name) {
        ChosenRestaurants chosenRestaurant = new ChosenRestaurants(placeId, name);
        return chosenRestaurantsListCollection.document().set(chosenRestaurant);
    }

    // ................................................................................... GET
    // TODO check if this will work
    public Task<QuerySnapshot> getChosenRestaurants() {
        return chosenRestaurantsListCollection.get();
    }

    // ................................................................................... UPDATE
    public Task<Void> updateChosenRestaurants(Restaurant chosenRestaurant) {
        return chosenRestaurantsListCollection.document().update("chosenRestaurants", chosenRestaurant);
    }

    // ................................................................................... DELETE
    public Task<Void> deleteChosenRestaurants() {
        return chosenRestaurantsListCollection.document().delete();
    }
}
