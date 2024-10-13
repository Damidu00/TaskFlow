package com.example.taskflow.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.taskflow.R
import com.example.taskflow.database.TodoDatabase
import com.example.taskflow.database.repositories.TodoRepository
import com.example.taskflow.model.Todo
import com.example.taskflow.view_model.MainActivityViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

class TodoAdapter(context:Context, todoList:List<Todo>, viewModel: MainActivityViewModel) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    private val context:Context
    private var todoList:List<Todo>
    private var databaseRepo:TodoRepository
    private var viewModel: MainActivityViewModel
    private var onItemEdit:OnItemEdit? = null

    interface OnItemEdit{
        fun onItemEdit(id:Int)
    }

    init{
        this.context = context
        this.todoList = todoList
        databaseRepo = TodoRepository(TodoDatabase.getInstance(context))
        this.viewModel = viewModel
    }

    fun setItemEditListener(onItemEdit:OnItemEdit){
        this.onItemEdit = onItemEdit
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.todo_item_view,parent,false)
        return TodoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return todoList.size
    }


    private fun setPriorityColor(priority: String, imageView: ImageView) {
        val color = when (priority) {
            "Very Low" -> Color.parseColor("#D5D378") // Light Yellow
            "Low" -> Color.parseColor("#CFCB23") // Dark Yellow
            "Normal" -> Color.parseColor("#1E9C2A") // Green
            "High" -> Color.parseColor("#CC6060") // Light Red
            "Very High" -> Color.parseColor("#E81919") // Dark Red
            else -> Color.TRANSPARENT // Default if priority doesn't match
        }

        // Get the drawable resource
        val drawable = ContextCompat.getDrawable(context, R.drawable.priority_circle)
        if (drawable != null) {
            // Change the color of the drawable
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            imageView.setImageDrawable(drawable) // Set the modified drawable
        }
    }



    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = todoList[position]
        holder.todoItemText.text = todo.title

        if (todo.description != "") {
            holder.todoItemDescription.text = todo.description
            holder.todoItemDescription.visibility = View.VISIBLE
        } else {
            holder.todoItemDescription.visibility = View.GONE
        }

        if (todo.deadlineDate != 0L) {
            holder.todoItemDeadline.text = "Deadline - ${getDateInMillis(todo.deadlineDate)}"
            holder.todoItemDeadline.visibility = View.VISIBLE
        } else {
            holder.todoItemDeadline.visibility = View.GONE
        }

        // Set the priority color
        setPriorityColor(todo.priority, holder.priority) // Call the function

        holder.itemView.setOnClickListener {
            // Your click handling code
        }

        holder.todoItemEditBtn.setOnClickListener {
            onItemEdit?.onItemEdit(todo.id!!.toInt())
        }

        holder.todoItemDeleteBtn.setOnClickListener {
            confirmDelete(todo, position)
        }
    }


    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val todoCardView: CardView = itemView.findViewById(R.id.todoCardView)
        val todoItemText: TextView = itemView.findViewById(R.id.todoItemText)
        val todoItemDescription: TextView = itemView.findViewById(R.id.todoItemDescription)
        val todoItemEditBtn: Button = itemView.findViewById(R.id.todoItemEditBtn)
        val todoItemDeleteBtn: Button = itemView.findViewById(R.id.todoItemDeleteBtn)
        val todoItemDeadline: TextView = itemView.findViewById(R.id.todoItemDeadline)
        val priority: ImageView = itemView.findViewById(R.id.priority)  // Add this line
    }


    private fun confirmDelete(todo: Todo,position:Int){

        MaterialAlertDialogBuilder(context)
            .setTitle("Confirm")
            .setMessage("Do you sure to delete this?")
            .setPositiveButton("Delete",DialogInterface.OnClickListener { dialogInterface, i ->
                deleteTodoItem(todo,position)
            }).setNegativeButton("Cancel",DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
            }).show()

    }

    private fun deleteTodoItem(todo: Todo, position:Int){
        CoroutineScope(Dispatchers.IO).launch {
            databaseRepo.delete(todo)
            val list = databaseRepo.getAllTodo()
            withContext(Dispatchers.Main){
                viewModel.setTodoList(list)
                Toast.makeText(context,"Item deleted",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getDateInMillis(millis:Long):String{
        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
        return simpleDateFormat.format(Date(millis))
    }

}