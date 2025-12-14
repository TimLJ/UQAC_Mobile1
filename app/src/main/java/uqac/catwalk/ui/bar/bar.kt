package uqac.catwalk.ui.bar

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uqac.catwalk.AchievementsActivity
import uqac.catwalk.CatListActivity
import uqac.catwalk.MainActivity
import uqac.catwalk.R
import uqac.catwalk.ShopActivity
import uqac.catwalk.WalkActivity
import uqac.catwalk.sauvegarde.MsMoney
import uqac.catwalk.sauvegarde.PlayerData

// Top bar for every screen that is not the main screen
@Composable
fun AppTopBar(
    context: Context,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val Player by remember { mutableStateOf(PlayerData) }
    Box(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .background(colorResource(R.color.light_yellow))
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bouton Home en haut à gauche
            Button(
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.light_blue)
                ),
                modifier = Modifier
                    .padding(start = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Retour à l'accueil",
                    tint = colorResource(R.color.black),
                )
            }
            // Barre d'experience
            Button(
                onClick = {
                    val intent = Intent(context, AchievementsActivity::class.java)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .size(200.dp, 80.dp)
                    .padding(start = 5.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.exp_bar),
                    contentDescription = "Barre d'expérience, qui ressemble à une fiole avec du liquide violet.",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(R.color.light_yellow))
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .padding(end = 10.dp)
                ) {
                    Text(
                        text = Player.money.toString(),
                        textAlign = TextAlign.Center,
                        fontSize = 25.sp,
                        modifier = Modifier
                            .size(height = 40.dp, width = 80.dp)
                            .padding(start = 15.dp)
                            .offset(25.dp)
                            .border(BorderStroke(2.dp, colorResource(R.color.orange)))
                            .background(colorResource(R.color.white))
                            .offset(x = 5.dp, y = 5.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.paw_coin),
                        contentDescription = "Image de pièce chat",
                        modifier = Modifier
                            .size(55.dp)
                    )
                }
            }

        }
        Image(
            painter = painterResource(R.drawable.lvl1),
            contentDescription = "Niveau un",
            modifier = Modifier
                .size(65.dp, 55.dp)
                .offset(x = 80.dp)
        )
    }
}

// Top bar of the app containing a button for the achievements, a coin image and the amount of coins the player has
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    context: Context,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(colorResource(R.color.light_yellow)),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Bouton succès
        Button(
            onClick = {
                context.startActivity(
                    Intent(context, AchievementsActivity::class.java)
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Succès",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        // Barre d'expérience
        Button(
            onClick = {
                context.startActivity(
                    Intent(context, AchievementsActivity::class.java)
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .weight(3f)
                .height(70.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(R.drawable.exp_bar),
                    contentDescription = "Barre d'expérience",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxSize()
                )
                Image(
                    painter = painterResource(R.drawable.lvl1),
                    contentDescription = "Niveau 1",
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.TopStart)
                )
            }
        }

        // Argent
        Box(
            modifier = Modifier
                .weight(1.5f),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = MsMoney.toString(),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(start = 30.dp)
                        .border(2.dp, colorResource(R.color.orange))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
                Image(
                    painter = painterResource(R.drawable.paw_coin),
                    contentDescription = "Pièce",
                    modifier = Modifier
                        .size(45.dp)
                        .align(Alignment.CenterStart)
                )
            }
        }
    }
}


// Bottom bar of the app containg buttons for the shop, the list of cats, and starting a walk
@Composable
fun AppBottomBar(
    context: Context,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        // Bottom navigation bar taking 1/8 of screen height
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(R.color.light_yellow))
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Shop section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        val intent = Intent(context, ShopActivity::class.java)
                        context.startActivity(intent)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "Boutique",
                        tint = colorResource(R.color.black)
                    )
                    Text("Boutique", color = colorResource(R.color.black))
                }
            }

            VerticalDivider(
                color = colorResource(R.color.black),
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxHeight()
            )

            // Cat section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        val intent = Intent(context, CatListActivity::class.java)
                        context.startActivity(intent)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.cat_icon),
                        contentDescription = "Icone de chat noir",
                        modifier = Modifier
                            .size(24.dp)
                    )
                    Text("Chats", color = colorResource(R.color.black))
                }
            }

            VerticalDivider(
                color = colorResource(R.color.black),
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxHeight()
            )

            // Walk section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        val intent = Intent(context, WalkActivity::class.java)
                        context.startActivity(intent)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.walk_icon),
                        contentDescription = "Chat noir de profil qui marche.",
                        modifier = Modifier
                            .size(28.dp)
                            .offset(y = -4.dp)
                    )
                    Text(
                        "Marche",
                        color = colorResource(R.color.black),
                        modifier = Modifier
                            .offset(y = -2.dp)
                    )
                }
            }
        }
    }
}