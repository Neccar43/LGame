package com.abdulkerim.composelgame

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.abdulkerim.composelgame.ui.theme.ComposeLGameTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeLGameTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    GameBoard()
                }
            }
        }
        //Uygulamanın sadece dikey modda kullanılmasını sağlıyor
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameBoard() {
    var boardSize by remember { mutableStateOf(7) }
    var score by remember { mutableStateOf(0) }
    //Score board
    Text(
        text = "Score: $score", fontSize = 35.sp, textAlign = TextAlign.Center,
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
    )
    //Oyun Alanı
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp)
    ) {

        val possibleMoves = remember { mutableStateListOf<Int>() }
        var firstMove by remember { mutableStateOf(true) }
        var highlightedBoxes by remember { mutableStateOf(0) }
        Surface(color = Color.Black) {
            LazyVerticalGrid(GridCells.Fixed(boardSize), content = {
                items(boardSize * boardSize) { index ->
                    var isEnabled by remember { mutableStateOf(true) }
                    var selectedBox by remember { mutableStateOf<Int?>(null) }
                    val isSelected = selectedBox == index
                    val isHighlighted = possibleMoves.contains(index)
                    var counter by remember { mutableStateOf(0) }

                    if (score == 0) {
                        isEnabled = true
                        selectedBox = null
                    }

                    LaunchedEffect(isHighlighted, isEnabled) {
                        if (isHighlighted && isEnabled) {
                            highlightedBoxes++
                        }
                    }

                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .aspectRatio(1f)
                            .clickable(enabled = isEnabled) {
                                if (isHighlighted || firstMove) {
                                    isEnabled = false
                                    selectedBox = index
                                    possibleMoves.clear()
                                    possibleMoves.addAll(
                                        calculateMove(
                                            selectedBox!!,
                                            boardSize
                                        )
                                    )
                                    firstMove = false
                                    score++
                                    counter = score
                                    highlightedBoxes=0
                                }
                            }
                            .background(
                                color = when {
                                    isSelected -> Color.Green
                                    isHighlighted -> Color.Yellow
                                    else -> Color.White
                                }
                            )
                    ) {
                        if (isSelected) {
                            Text(
                                counter.toString(), fontSize = when (boardSize) {
                                    7, 8, 9 -> 30.sp
                                    12 -> 25.sp
                                    15 -> 19.sp
                                    else -> 0.sp
                                }
                            )
                        }
                    }
                }
            })

        }

        val sizeMap = hashMapOf("7x7" to 7, "8x8" to 8, "9x9" to 9, "12x12" to 12, "15x15" to 15)
        Row {
            //Grid boyutunu seçmek için hazırlanan radyo düğmeleri
            var sizeState by remember { mutableStateOf("7x7") }//default
            val boardSizeList = remember { mutableListOf("7x7", "8x8", "9x9", "12x12", "15x15") }
            var isRadioAlertVisible by remember { mutableStateOf(false) }
            Column(modifier = Modifier.padding(top = 10.dp).fillMaxHeight()) {
                boardSizeList.forEach {size->
                    Row(
                        modifier = Modifier
                            .background(Color.White)
                            .padding(9.dp)
                    ) {
                        Text(text = size, fontSize = 24.sp, modifier = Modifier.fillMaxWidth(0.2f))
                        RadioButton(selected = sizeState == size,
                            onClick = {
                                sizeState = size
                                if (score != 0) {
                                    isRadioAlertVisible = true
                                }else{

                                    //Grid boyutunu değiştirir
                                    boardSize = sizeMap.getValue(sizeState)
                                }

                            })
                        if (isRadioAlertVisible) {
                            AlertDialog(
                                onDismissRequest = { isRadioAlertVisible = false },
                                title = { Text(text = "Uyarı") },
                                text = {
                                    Column {
                                        Text(text = "Oyun boyutunu değiştirmek istediğinize emin misiniz?")
                                    }
                                },
                                confirmButton = {
                                    Button(onClick = {
                                        //Grid boyutunu değiştirir
                                        boardSize = sizeMap.getValue(sizeState)
                                        isRadioAlertVisible=false
                                        score = 0
                                        possibleMoves.clear()
                                        firstMove = true
                                    }) {
                                        Text(text = "Evet")
                                    }
                                },
                                dismissButton = {
                                    Button(onClick = {
                                        isRadioAlertVisible = false
                                    }) {
                                        Text(text = "Hayır")
                                    }
                                }
                            )
                        }
                    }
                }
            }
            Column {
                var isAlertDialogVisible by remember { mutableStateOf(false) }
                //Reset düğmesi
                Button(modifier = Modifier.padding(10.dp),
                    onClick = {
                        if (score != 0) {
                            isAlertDialogVisible = true
                        }
                    }
                ) {
                    Text(
                        text = "Yeniden Dene",
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                //Reset için uyarı mesajı
                if (isAlertDialogVisible) {
                    AlertDialog(
                        onDismissRequest = { isAlertDialogVisible = false },
                        title = { Text(text = "Uyarı") },
                        text = {
                            Column {
                                Text(text = "Mevcut oyununuzu sıfırlamak istediğinize emin misiniz?")
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                score = 0
                                possibleMoves.clear()
                                firstMove = true
                                isAlertDialogVisible = false
                            }) {
                                Text(text = "Evet")
                            }
                        },
                        dismissButton = {
                            Button(onClick = {
                                isAlertDialogVisible = false
                            }) {
                                Text(text = "Hayır")
                            }
                        }
                    )
                }

                //Açıklama yazısı
                Text(
                    text = if (firstMove) "Başlangıç hamlenizi yapınız." else if(highlightedBoxes==0) "Oyun Bitti." else "Muhtemel $highlightedBoxes hareket mevcut.",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxWidth()

                )
            }

        }


    }


}

//Kullanıcı hamlelerini hesaplayan fonksiyon
fun calculateMove(position: Int, boardSize: Int): List<Int> {
    //Pozisyon değerleri x ve y'ye dönüştürülüyor
    val x = position % boardSize
    val y = position / boardSize

    //8 hamlenin hesaplanıp x y ikililerinin listesi oluşturuluyor
    val moves = listOf(
        Pair(x + 1, y - 2), Pair(x + 2, y - 1),
        Pair(x + 2, y + 1), Pair(x + 1, y + 2),
        Pair(x - 1, y + 2), Pair(x - 2, y + 1),
        Pair(x - 2, y - 1), Pair(x - 1, y - 2)
    )
    //Kordinat düzleminin dışında kalan hamleler filtreleniyor
    return moves.filter { it.first in 0 until boardSize && it.second in 0 until boardSize }
        //x y'li kordinatlar pozisyon değerlerine çevrilip döndürülüyor
        .map { it.second * boardSize + it.first }
}

//Uygulamayı ön izlemek için preview oluşturma
@Preview
@Composable
fun BoardPreview() {
    GameBoard()
}








