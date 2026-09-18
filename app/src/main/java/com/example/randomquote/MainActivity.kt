package com.example.randomquote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.randomquote.data.QuoteRepository
import com.example.randomquote.ui.QuoteScreen
import com.example.randomquote.ui.QuoteViewModel
import com.example.randomquote.ui.theme.RandomQuoteTheme

class MainActivity : ComponentActivity() {

    private val viewModel: QuoteViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = QuoteRepository(applicationContext)
                return QuoteViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RandomQuoteTheme {
                QuoteScreen(viewModel = viewModel)
            }
        }
    }
}
