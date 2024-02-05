@file:OptIn(ExperimentalMaterial3Api::class)

package bek.droid.swipetodelete

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import bek.droid.swipetodelete.ui.theme.SwipeToDeleteTheme
import kotlinx.coroutines.delay
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwipeToDeleteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val books = remember {
                        mutableStateListOf(
                            "Book 1",
                            "Book 2",
                            "Book 3",
                            "Book 4",
                            "Book 5",
                        )
                    }
                    val onDelete: (String) -> Unit = { book ->
                        books -= book
                        Toast.makeText(context, "$book is deleted!", Toast.LENGTH_SHORT).show()
                    }

                    val onSave: (String) -> Unit = { book ->
                        Toast.makeText(context, "$book is saved!", Toast.LENGTH_SHORT).show()
                    }

                    BooksList(
                        modifier = Modifier.fillMaxSize(),
                        books = books,
                        onDelete = onDelete,
                        onSave = onSave
                    )
                }
            }
        }
    }
}

@Composable
fun BooksList(
    modifier: Modifier,
    books: List<String>,
    onDelete: (String) -> Unit,
    onSave: (String) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(items = books, key = { it }) { book ->
            BookItem(
                book = book,
                onDelete = onDelete,
                onSave = onSave,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun BookItem(
    modifier: Modifier = Modifier,
    book: String,
    onDelete: (String) -> Unit,
    onSave: (String) -> Unit
) {
    SwipeDeleteContainer(item = book, onDelete = onDelete, onSave = onSave) {
        Text(
            text = book,
            modifier = modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
    Divider(Modifier.fillMaxWidth())
}

@Composable
fun <T> SwipeDeleteContainer(
    item: T,
    onDelete: (T) -> Unit,
    onSave: (T) -> Unit,
    animationDuration: Int = 500,
    content: @Composable (T) -> Unit
) {
    var isRemoved by remember {
        mutableStateOf(false)
    }
    var isSave by remember {
        mutableStateOf(false)
    }

    val state = rememberDismissState(
        confirmValueChange = { value: DismissValue ->
            when (value) {
                DismissValue.DismissedToStart -> {
                    isRemoved = true
                    true
                }

                DismissValue.DismissedToEnd -> {
                    isSave = true
                    false
                }

                else -> {
                    false
                }
            }
        }
    )

    LaunchedEffect(key1 = isRemoved, key2 = isSave) {
        if (isRemoved) {
            delay(animationDuration.toLong())
            onDelete(item)
        }

        if (isSave) {
            onSave(item)
        }
    }

    AnimatedVisibility(
        visible = !isRemoved,
        exit = shrinkVertically(
            animationSpec = tween(animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismiss(
            state = state,
            background = {
                DeleteBackground(swipeDismissState = state)
            },
            dismissContent = {
                content(item)
            },
//            directions = setOf(DismissDirection.EndToStart,DismissDirection.StartToEnd)
        )
    }
}

@Composable
fun DeleteBackground(
    swipeDismissState: DismissState
) {
    val color = when (swipeDismissState.dismissDirection) {
        DismissDirection.EndToStart -> {
            Color.Red
        }

        DismissDirection.StartToEnd -> {
            Color.Green
        }

        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            modifier = Modifier.align(
                Alignment.CenterEnd
            ),
            tint = Color.White
        )
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.align(
                Alignment.CenterStart
            ),
            tint = if (swipeDismissState.dismissDirection == DismissDirection.EndToStart)
                Color.Red
            else Color.White
        )
    }
}