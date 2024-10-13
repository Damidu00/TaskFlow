package com.example.taskflow.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taskflow.model.Todo

class MainActivityViewModel : ViewModel() {

    private val mTodoList = MutableLiveData<List<Todo>>()
    var todoList: LiveData<List<Todo>> = mTodoList

    fun setTodoList(todoList:List<Todo>){
        mTodoList.value = todoList
    }

}