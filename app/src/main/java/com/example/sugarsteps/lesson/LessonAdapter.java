package com.example.sugarsteps.lesson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sugarsteps.R;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private final List<Lesson> lessonList;  // List of lessons to display
    private final Context context;           // Context for inflating views and launching activities
    private OnItemLongClickListener longClickListener; // For clicking long on the lesson (for editing)

    // Adapter constructor
    public LessonAdapter(Context context, List<Lesson> lessonList) {
        this.context = context;
        this.lessonList = lessonList;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the lesson item layout (lesson_item.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.lesson_item, parent, false);
        return new LessonViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility") // Makes android ignore no adequate info for accessibility
    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        // Get the current lesson
        Lesson lesson = lessonList.get(position);

        // Bind lesson data to the views
        holder.lessonTitle.setText(lesson.getLessonName());
        holder.lessonDescription.setText(lesson.getShortDescription());
        holder.lessonImage.setImageURI(Uri.parse(lesson.getLessonPhoto()));

        // Set like button image and tag based on model
        if (lesson.isLiked()) {
            holder.likeButton.setImageResource(R.drawable.ic_heart_full);
            holder.likeButton.setTag(true);
        } else {
            holder.likeButton.setImageResource(R.drawable.ic_heart_border);
            holder.likeButton.setTag(false);
        }

        // Handle like button click to toggle heart icon
        holder.likeButton.setOnClickListener(v -> {
            boolean liked = lesson.isLiked();
            lesson.setLiked(!liked);

            // Get viewModel from context
            if (context instanceof AppCompatActivity) {
                LessonsViewModel viewModel = new ViewModelProvider((AppCompatActivity) context)
                        .get(LessonsViewModel.class);
                viewModel.update(lesson); // Update data base in live
            }

            notifyItemChanged(position); //  Refresh UI
        });


        // Set checkbox checked state from the model
        holder.lessonCheckBox.setChecked(lesson.isCheck());

        // Enable the click so it will able to make a toast about clicking it
        holder.lessonCheckBox.setClickable(true); // Make it clickable
        holder.lessonCheckBox.setFocusable(false); // Prevent focus shift on click

        // Add click listener to checkbox to show toast
        holder.lessonCheckBox.setOnClickListener(v -> {
            boolean attemptedChecked = holder.lessonCheckBox.isChecked();

            // Revert the checkbox to its real state from DB
            holder.lessonCheckBox.setChecked(lesson.isCheck());

            // Show different toast based on what the user tried to do
            if (attemptedChecked) {
                Toast.makeText(context, "אפשר לסמן 'בוצע' רק מתוך השיעור עצמו", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "אפשר לבטל סימון רק מתוך השיעור עצמו", Toast.LENGTH_SHORT).show();
            }
        });

        // Change background1 if the lesson is done
        if(holder.lessonCheckBox.isChecked())
        {
            int color = ContextCompat.getColor(context, R.color.light_peach);
            holder.cardRelative.setBackgroundColor(color);
        }
        else
        {
            int color = ContextCompat.getColor(context, R.color.peach);
            holder.cardRelative.setBackgroundColor(color);
        }

        // Navigate to lesson detail when item is clicked
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, LessonDetailActivity.class);
            intent.putExtra("lessonId", lesson.getLessonId()); // Pass lesson ID to retrieve data from DB
            context.startActivity(intent);
        });

        // Trigger long click listener (used for editing)
        holder.cardView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(lesson.getLessonId());
                return true;
            }
            return false;
        });

    }

    // Retrieve lesson by position (used for swipe-to-delete)
    public Lesson getLessonAt(int position) {
        return lessonList.get(position);
    }

    // Return total number of items
    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    // Interface for long click behavior (used for edit)
    public interface OnItemLongClickListener {
        void onItemLongClick(int lessonId);
    }

    // Setter for the long click listener
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    // ViewHolder class to hold views for each item
    static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView lessonTitle, lessonDescription;
        ImageView lessonImage;
        ImageButton likeButton;
        CheckBox lessonCheckBox;
        CardView cardView;
        RelativeLayout cardRelative;

        LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            lessonTitle = itemView.findViewById(R.id.tx_lesson_title);
            lessonDescription = itemView.findViewById(R.id.tx_lesson_description);
            lessonImage = itemView.findViewById(R.id.img_lesson_photo);
            likeButton = itemView.findViewById(R.id.imgbtn_like);
            lessonCheckBox = itemView.findViewById(R.id.btn_lesson_done);
            cardView = itemView.findViewById(R.id.card_lesson);
            cardRelative = itemView.findViewById(R.id.relay_allcard);
            likeButton.setTag(false); // Default: not liked
        }

    }
}
