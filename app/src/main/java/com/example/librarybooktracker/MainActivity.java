package com.example.librarybooktracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    EditText bookCodeEditText, daysBorrowedEditText, bookTitleEditText, authorEditText, totalPriceEditText;
    Button  borrowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bookCodeEditText = findViewById(R.id.editTextBookCode);
        daysBorrowedEditText = findViewById(R.id.editTextDaysBorrowed);
        bookTitleEditText = findViewById(R.id.editTextBookTitle);
        authorEditText = findViewById(R.id.editTextAuthor);
        totalPriceEditText = findViewById(R.id.editTextTotalPrice);
        borrowButton = findViewById(R.id.buttonBorrow);


        borrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bookCode = bookCodeEditText.getText().toString();

                db.collection("Books")
                        .whereEqualTo("code", bookCode)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots.isEmpty()){
                                    bookTitleEditText.setText("No Book Found");
                                    authorEditText.setText("No Book Found");
                                    totalPriceEditText.setText("No Book Found");
                                }
                                else{
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        String code = documentSnapshot.getString("code");
                                        String author = documentSnapshot.getString("author");
                                        String title = documentSnapshot.getString("title");
                                        String type = documentSnapshot.getString("type");
                                        boolean isBorrowed = documentSnapshot.getBoolean("isBorrowed");

                                        Book book = new Premium(code, author, title, isBorrowed);
                                        int days = Integer.parseInt(daysBorrowedEditText.getText().toString());
                                        double totalPrice = 0;

                                        if (!book.getIsBorrowed()){
                                            if (type.equals("regular")){
                                                totalPrice = book.regular(days);
                                            }
                                            else{
                                                totalPrice = book.premium(days);
                                            }

                                            bookTitleEditText.setText(book.getTitle());
                                            authorEditText.setText(book.getAuthor());
                                            totalPriceEditText.setText(String.valueOf(totalPrice));
                                        }
                                        else{
                                            bookTitleEditText.setText(book.getTitle());
                                            authorEditText.setText(book.getAuthor());
                                            totalPriceEditText.setText("Already Borrowed");
                                        }
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("TAG", e.toString());
                            }
                        });
            }
        });
    }

    interface Book{
        double regular(int days);
        double premium(int days);
        boolean getIsBorrowed();
        String getTitle();
        String getAuthor();
    }

    abstract class Regular implements Book {
        private String bookCode;
        private String title;
        private String author;
        private int numberOfDaysBorrowed;
        private boolean isBorrowed;

        Regular(){
            bookCode = "";
            title = "";
            author = "";
            numberOfDaysBorrowed = 0;
            isBorrowed = false;
        }

        Regular(String bookCode, String title, String author, boolean isBorrowed){
            this.bookCode = bookCode;
            this.title = title;
            this.author = author;
            this.numberOfDaysBorrowed = numberOfDaysBorrowed;
            this.isBorrowed = isBorrowed;
        }

        public double regular(int days){
            return days * 20.00;
        }

        public String getTitle(){
            return title;
        }

        public String getAuthor(){
            return author;
        }

        public boolean getIsBorrowed(){
            return isBorrowed;
        }
    }

    class Premium extends Regular{

        Premium(){
            super();
        }

        Premium(String bookCode, String title, String author, boolean isBorrowed){
            super(bookCode, title, author, isBorrowed);
        }

        public double premium(int days){
            double price = 50.00;
            double totalPrice = 0;
            int tempDays = 0;

            if (days > 7){
                tempDays = days - 7;
                totalPrice += (tempDays * price) + (tempDays * 25.00) + (7 * price);
            }
            else{
                totalPrice = days * price;
            }

            return totalPrice;
        }
    }
}