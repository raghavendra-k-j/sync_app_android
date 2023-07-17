package com.example.syncapp.activities.books;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syncapp.databinding.ListItemBookBinding;
import com.example.syncapp.entity.Book;

import java.util.ArrayList;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {

    private Context context;

    private ArrayList<Book> bookArrayList;

    private LayoutInflater layoutInflater;

    BookItemActionListener bookItemActionListener;

    public BooksAdapter(Context context, ArrayList<Book> bookArrayList) {
        this.context = context;
        this.bookArrayList = bookArrayList;
        this.layoutInflater = LayoutInflater.from(context);
        bookItemActionListener = (BookItemActionListener) context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<Book> items) {
        this.bookArrayList.clear();
        this.bookArrayList.addAll(items);
        notifyDataSetChanged();
    }

    public void updateItem(Book book, int position) {
        this.bookArrayList.set(position, book);
        notifyItemChanged(position);
    }

    public void removeItem(Book book, int position) {
        this.bookArrayList.remove(book);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public BooksAdapter.BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookViewHolder(ListItemBookBinding.inflate(layoutInflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BooksAdapter.BookViewHolder holder, int position) {
        Book book = bookArrayList.get(position);
        holder.binding.listItemBookAuthor.setText(book.getTitle());
        holder.binding.listItemBookContent.setText(book.getContent());
        holder.binding.listItemBookTitle.setText(book.getTitle());
        holder.binding.getRoot().setOnClickListener(v -> bookItemActionListener.onEditBookClicked(book, position));
    }

    @Override
    public int getItemCount() {
        return bookArrayList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ListItemBookBinding binding;
        public BookViewHolder(@NonNull ListItemBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    interface BookItemActionListener {
        void onEditBookClicked(Book book, int position);
    }
}
