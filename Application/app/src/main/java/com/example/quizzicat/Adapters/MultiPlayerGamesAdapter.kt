package com.example.quizzicat.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quizzicat.Facades.ImageLoadingFacade
import com.example.quizzicat.Facades.MultiPlayerDataRetrievalFacade
import com.example.quizzicat.Facades.TopicsDataRetrievalFacade
import com.example.quizzicat.Facades.UserDataRetrievalFacade
import com.example.quizzicat.Model.*
import com.example.quizzicat.R
import com.example.quizzicat.Utils.CounterCallBack
import com.example.quizzicat.Utils.MultiPlayerGamesCallBack
import com.example.quizzicat.Utils.TopicCallBack
import com.example.quizzicat.Utils.UserDataCallBack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MultiPlayerGamesAdapter(
    private val mainContext: Context?,
    private val firebaseFirestore: FirebaseFirestore,
    private var list: ArrayList<MultiPlayerUserJoined>): RecyclerView.Adapter<MultiPlayerGamesAdapter.MultiPlayerGamesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiPlayerGamesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MultiPlayerGamesViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MultiPlayerGamesViewHolder, position: Int) {
        val multiPlayerGame = list[position]
        holder.bind(firebaseFirestore, mainContext!!, multiPlayerGame)
    }

    class MultiPlayerGamesViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.view_multi_player_game, parent, false)) {

        private var multi_game_topic_icon: ImageView? = null
        private var multi_game_topic_host: TextView? = null
        private var multi_game_date: TextView? = null
        private var multi_game_points: TextView? = null
        private var multi_game_winner: ImageView? = null

        init {
            multi_game_topic_icon = itemView.findViewById(R.id.view_multi_game_topic)
            multi_game_topic_host = itemView.findViewById(R.id.view_multi_game_host)
            multi_game_date = itemView.findViewById(R.id.view_multi_game_date)
            multi_game_points = itemView.findViewById(R.id.view_multi_game_points)
            multi_game_winner = itemView.findViewById(R.id.view_multi_game_winner)
        }

        fun bind(firebaseFirestore: FirebaseFirestore, mainContext: Context, game: MultiPlayerUserJoined) {
            val multiPlayerDataRetrievalFacade = MultiPlayerDataRetrievalFacade(firebaseFirestore, mainContext)
            multiPlayerDataRetrievalFacade
                .getMultiPlayerGameData(game.gid, object: MultiPlayerGamesCallBack {
                    override fun onCallback(value: ArrayList<MultiPlayerGame>) {
                        val multiGame = value[0]
                        TopicsDataRetrievalFacade(firebaseFirestore, mainContext)
                            .getTopicDetails(object: TopicCallBack {
                                override fun onCallback(value: Topic) {
                                    val topic = value
                                    if (FirebaseAuth.getInstance().uid != null) {
                                        UserDataRetrievalFacade(firebaseFirestore, multiGame.created_by)
                                            .getUserDetails(object: UserDataCallBack {
                                                override fun onCallback(value: User) {
                                                    val host = value
                                                    multiPlayerDataRetrievalFacade.hasUserWon(FirebaseAuth.getInstance().uid!!, multiGame.gid, object: CounterCallBack {
                                                        override fun onCallback(value: Int) {
                                                            if (value == 1)
                                                                multi_game_winner!!.visibility = View.VISIBLE
                                                            ImageLoadingFacade(mainContext).loadImage(topic.icon_url, multi_game_topic_icon!!)
                                                            multi_game_points!!.text = game.score.toString()
                                                            multi_game_topic_host!!.text = host.display_name
                                                            multi_game_date!!.text = multiGame.created_on
                                                        }
                                                    })
                                                }
                                            })

                                    }
                                }
                            }, multiGame.tid)
                    }
                })
        }
    }
}