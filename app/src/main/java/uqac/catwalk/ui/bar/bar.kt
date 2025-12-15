package uqac.catwalk.ui.bar

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
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
import uqac.catwalk.walk.WalkActivity
import uqac.catwalk.sauvegarde.PlayerData

// Top bar for every screen that is not the main screen
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = PlayerData

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(colorResource(R.color.light_yellow))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Bouton Home
        Button(
            onClick = {
                context.startActivity(Intent(context, MainActivity::class.java))
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.light_blue)
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Accueil",
                tint = colorResource(R.color.black)
            )
        }

        // Bouton Étoile
        IconButton(
            onClick = {
                context.startActivity(Intent(context, AchievementsActivity::class.java))
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Succès",
                tint = colorResource(R.color.orange),
                modifier = Modifier.size(40.dp)
            )
        }

        // Barre d’experience
        Box(
            modifier = Modifier
                .weight(4f)
                .height(100.dp)
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
                    contentDescription = "Niveau",
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.CenterStart)

                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Argent + icône
        Box(
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = player.money.toString(),
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(start = 30.dp)
                    .border(2.dp, colorResource(R.color.orange))
                    .background(colorResource(R.color.white))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            )

            Image(
                painter = painterResource(R.drawable.paw_coin),
                contentDescription = "Pièce",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

// Top bar of the app containing a button for the achievements, a coin image and the amount of coins the player has
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    context: Context,
    modifier: Modifier = Modifier
) {

    val player = PlayerData

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(colorResource(R.color.light_yellow)),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Bouton succès
        IconButton(
            onClick = {
                context.startActivity(
                    Intent(context, AchievementsActivity::class.java)
                )
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Succès",
                tint = colorResource(R.color.orange),
                modifier = Modifier.size(40.dp)
            )
        }

        // Barre d'expérience
        Box(
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
                    text = player.money.toString(),
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

// Bottom bar specific to the cat interaction screen containing buttons to play, wash, or pet the cat
@Composable
fun CatBottomBar(
    onPlay: () -> Unit,
    onWash: () -> Unit,
    onPet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(100.dp)
            .background(colorResource(R.color.light_yellow))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable {
                    onPlay()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Jouer",
                    tint = colorResource(R.color.black)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Jouer", color = colorResource(R.color.black))
            }
        }

        VerticalDivider(
            color = colorResource(R.color.black),
            thickness = 1.dp,
            modifier = Modifier.fillMaxHeight()
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable {
                    onWash()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.laver_icon),
                    contentDescription = "Laver",
                    modifier = Modifier.size(25.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Laver", color = colorResource(R.color.black))
            }
        }

        VerticalDivider(
            color = colorResource(R.color.black),
            thickness = 1.dp,
            modifier = Modifier.fillMaxHeight()
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable {
                    onPet()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FavoriteBorder,
                    contentDescription = "Caresser",
                    tint = colorResource(R.color.black)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Caresser", color = colorResource(R.color.black))
            }
        }
    }
}