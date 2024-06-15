package com.akardas16.infinitycircularcarousel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akardas16.infinitycircularcarousel.ui.theme.InfinityCircularCarouselTheme
import com.gigamole.infinitecycleviewpager.CarouselCompose
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        setContent {
            InfinityCircularCarouselTheme {
                val scope = rememberCoroutineScope()
                val verticalState = rememberPagerState(pageCount = {
                    locations.count()
                })
                var currentIndex by remember {
                    mutableIntStateOf(0)
                }
                val context = LocalContext.current


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF181624))
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    CarouselCompose(
                        modifier = Modifier
                            .weight(3f)
                            .padding(horizontal = 4.dp),
                        locations,
                        onChangedIndex = {
                            currentIndex = it
                            scope.launch { verticalState.animateScrollToPage(it) }

                        },
                        isAutoScroll = false,
                    ) { item: Any ->

                        (item as LocationItem).let {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = it.image),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            width = 2.dp,
                                            if (it.title == locations[currentIndex].title) gradientBlue() else gradientUnselect(),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .then(
                                            if (it.title == locations[currentIndex].title) {
                                                Modifier
                                            } else {
                                                Modifier.blur(20.dp)
                                            }
                                        )
                                )
                            }
                        }

                    }

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxHeight()
                            .weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        VerticalPager(
                            state = verticalState, modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            userScrollEnabled = false,
                            horizontalAlignment = Alignment.Start
                        ) { page ->
                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = locations[page].title,
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Thin,
                                        color = Color.White,
                                        fontSize = 28.sp
                                    )
                                )
                                Text(
                                    text = locations[page].subtitle,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }

                        RatingBar(rating = (currentIndex % 3).toFloat())

                    }
                }
            }

        }
    }
}

