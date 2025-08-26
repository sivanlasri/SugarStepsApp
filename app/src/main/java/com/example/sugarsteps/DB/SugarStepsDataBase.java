package com.example.sugarsteps.DB;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.sugarsteps.R;
import com.example.sugarsteps.lesson.Lesson;
import com.example.sugarsteps.lesson.LessonsDao;
import com.example.sugarsteps.user.User;
import com.example.sugarsteps.user.UserDao;

import java.util.concurrent.Executors;

@Database(entities = {User.class, Lesson.class}, version = 15, exportSchema = false)
// Annotate the class as a Room database including the entities it manages and the version number.
public abstract class SugarStepsDataBase extends RoomDatabase {

    // Declare abstract methods to get DAO instances for each entity.
    public abstract UserDao usersDao();
    public abstract LessonsDao lessonsDao();

    // Singleton instance to prevent having multiple instances of the database opened at the same time.
    private static volatile SugarStepsDataBase INSTANCE;

    // Method to get the database instance.
    public static SugarStepsDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SugarStepsDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    SugarStepsDataBase.class,
                                    "sugarsteps_database"
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@androidx.annotation.NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);

                                    // Inserting default lessons
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        SugarStepsDataBase database = getDatabase(context);
                                        LessonsDao dao = database.lessonsDao();

                                        dao.insertLesson(new Lesson( "חלות עם ריח ממכר", // Lesson title
                                                "android.resource://com.example.sugarsteps/" + R.drawable.halot_lesson,               // Lesson image resource
                                                "חלות שלא תרצו להפסיק להכין",        // Lesson short description
                                                "סיון לסרי",                          // Author
                                                "מומחים",                            // Level (note: in filter use matching string)
                                                "android.resource://com.example.sugarsteps/" + R.raw.halot,  // Audio/video resource URI
                                                "android.resource://com.example.sugarsteps/" + R.raw.halot_recipe  // Url to text file
                                                ));
                                        dao.insertLesson(new Lesson("קאפקייקס נימוחים", // Lesson title
                                                "android.resource://com.example.sugarsteps/" + R.drawable.cupcakes_lesson,               // Lesson image resource
                                                "קאפקייקס חלום שקל להכין!",        // Lesson short description
                                                "סיון לסרי",                          // Author
                                                "מתחילים",                            // Level (note: in filter use matching string)
                                                "android.resource://com.example.sugarsteps/" + R.raw.cupcakes,  // Audio/video resource URI
                                                "android.resource://com.example.sugarsteps/" + R.raw.cupcakes_recipe   // Url to text file
                                                ));
                                        dao.insertLesson(new Lesson("עוגיות שוקולד צ׳יפס", // Lesson title
                                                "android.resource://com.example.sugarsteps/" + R.drawable.chocolate_chips_lesson,               // Lesson image resource
                                                "המתכון הקלאסי לעוגיות שלמות!",        // Lesson short description
                                                "סיון לסרי",                          // Author
                                                "מתחילים",                            // Level (note: in filter use matching string)
                                                "android.resource://com.example.sugarsteps/" + R.raw.chocalate_chips_cookies,  // Audio/video resource URI
                                                "android.resource://com.example.sugarsteps/" + R.raw.chocalate_chips_recipe   // Url to text file
                                        ));
                                        dao.insertLesson(new Lesson("עוגת תפוזים רכה", // Lesson title
                                                "android.resource://com.example.sugarsteps/" + R.drawable.orange_cake_lesson,               // Lesson image resource
                                                "ה-מתכון לעוגת תפוזים אוורירית",        // Lesson short description
                                                "סיון לסרי",                          // Author
                                                "מתקדמים",                            // Level (note: in filter use matching string)
                                                "android.resource://com.example.sugarsteps/" + R.raw.orange_cake,  // Audio/video resource URI
                                                "android.resource://com.example.sugarsteps/" + R.raw.orange_cake_recipe  // Url to text file
                                        ));
                                        dao.insertLesson(new Lesson("עוגת שמרים שעושה חשק", // Lesson title
                                                "android.resource://com.example.sugarsteps/" + R.drawable.shmarim_cake_lesson,               // Lesson image resource
                                                "מתכון הכי מקצועי שיש לשמרים",        // Lesson short description
                                                "סיון לסרי",                          // Author
                                                "מומחים",                            // Level (note: in filter use matching string)
                                                "android.resource://com.example.sugarsteps/" + R.raw.shmarim_cake,  // Audio/video resource URI
                                                "android.resource://com.example.sugarsteps/" + R.raw.shmarim_cake_recipe  // Url to text file
                                        ));
                                        dao.insertLesson(new Lesson("עוגת קרמבו", // Lesson title
                                                "android.resource://com.example.sugarsteps/" + R.drawable.three_layers_cake_lesson,               // Lesson image resource
                                                "עוגה שלא תרצו להפסיק להכין",        // Lesson short description
                                                "סיון לסרי",                          // Author
                                                "מומחים",                            // Level (note: in filter use matching string)
                                                "android.resource://com.example.sugarsteps/" + R.raw.three_layers_cake,  // Audio/video resource URI
                                                "android.resource://com.example.sugarsteps/" + R.raw.three_layers_cake_recipe  // Url to text file
                                        ));
                                        dao.insertLesson(new Lesson("עוגת גבינה פירורים", // Lesson title
                                                "android.resource://com.example.sugarsteps/" + R.drawable.cheese_cake_lesson,               // Lesson image resource
                                                "מתכון שלא ייצא לכם מהראש",        // Lesson short description
                                                "סיון לסרי",                          // Author
                                                "מתקדמים",                            // Level (note: in filter use matching string)
                                                "android.resource://com.example.sugarsteps/" + R.raw.cheese_cake,  // Audio/video resource URI
                                                "android.resource://com.example.sugarsteps/" + R.raw.cheese_cake_recipe  // Url to text file
                                        ));
                                        dao.insertLesson(new Lesson("מגולגלות תמרים", // Lesson title
                                                "android.resource://com.example.sugarsteps/" + R.drawable.megolgalot_tmarim_lesson,               // Lesson image resource
                                                "עוגיות שרק בא לחסל",        // Lesson short description
                                                "סיון לסרי",                          // Author
                                                "מתחילים",                            // Level (note: in filter use matching string)
                                                "android.resource://com.example.sugarsteps/" + R.raw.megolgalot_tmarim,  // Audio/video resource URI
                                                "android.resource://com.example.sugarsteps/" + R.raw.megolgalot_tmarim_recipe  // Url to text file
                                        ));
                                        dao.insertLesson(new Lesson("רולדה ריבת חלב", // Lesson title
                                                "android.resource://com.example.sugarsteps/" + R.drawable.milk_jam_lesson,               // Lesson image resource
                                                "טעם בלתי נשכח",        // Lesson short description
                                                "סיון לסרי",                          // Author
                                                "מתקדמים",                            // Level (note: in filter use matching string)
                                                "android.resource://com.example.sugarsteps/" + R.raw.milk_jam_roll,  // Audio/video resource URI
                                                "android.resource://com.example.sugarsteps/" + R.raw.milk_jam_roll_recipe  // Url to text file
                                        ));
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
