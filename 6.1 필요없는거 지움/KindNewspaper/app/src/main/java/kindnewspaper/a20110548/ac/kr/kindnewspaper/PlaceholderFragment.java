package kindnewspaper.a20110548.ac.kr.kindnewspaper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class PlaceholderFragment extends Fragment implements AdapterView.OnItemClickListener {
    private int mPageNumber;

    public static final String NEWSTAG = "NewsTag";

    private String mResult = null;

    private ArrayList<NewsInfo> mArray = new ArrayList<NewsInfo>();

    private ListView mList = null;
    private NewsAdapter mAdapter = null;

    private String title = "";
    private String content = "";
    private String author = "";
    private String link = "";


    private XmlPullParserFactory xmlFactoryObject;
    public static final Semaphore mutex = new Semaphore(1);


    private RequestQueue mQueue = null;
    private ImageLoader mImageLoader = null;

    private int newscompany = -1;
    private int category = -1;
    private String[][] allURL = new String[2][3];

    protected ArrayList<NewsInfo> allArray = new ArrayList<NewsInfo>();
    SearchView searchView = null;
    PlanetFilter planetFilter = null;
    protected ArrayList<NewsInfo> tmpArray = new ArrayList<NewsInfo>();
    protected ArrayList<NewsInfo> tmpArray2 = new ArrayList<NewsInfo>();
    public static final int RssSize = 50;
    public PlaceholderFragment() {
    }

    public static PlaceholderFragment create(int pageNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt("page", pageNumber);
        fragment.setArguments(args);
        return fragment;
    } //인스턴스 !!!

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt("page");

        Cache cache = new DiskBasedCache(this.getContext().getCacheDir(), 1024 * 1024); // 1MB
        Network network = new BasicNetwork(new HurlStack());
        mQueue = new RequestQueue(cache, network);
        mQueue.start();

        mImageLoader = new ImageLoader(mQueue, new LruBitmapCache(LruBitmapCache.getCacheSize(this.getContext())));

        settingAllURL();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.main,menu);

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                    mAdapter.getFilter().filter(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s.equals(""))
                    mAdapter.getFilter().filter(s);
//Toast.makeText(MainActivity.this,s,Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    public void settingAllURL() {
        //이데일리
        allURL[0][0] = "http://rss.edaily.co.kr/edaily_news.xml"; //전체
        allURL[0][1] = "http://rss.edaily.co.kr/ent_news.xml"; //연예(문화)
        allURL[0][2] = "http://rss.edaily.co.kr/sports_news.xml"; //스포츠

        //한국경제TV
        allURL[1][0] = "http://www.wowtv.co.kr/listTotal.xml"; //전체
        allURL[1][1] = "http://www.wowtv.co.kr/listEnt.xml"; //연예(문화)
        allURL[1][2] = "http://www.wowtv.co.kr/listSports.xml"; //스포츠

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.content_main, container, false);
        category = mPageNumber;
        requestNews();
        mAdapter = new NewsAdapter(this.getContext(), R.layout.news_list);
        mList = (ListView) rootView.findViewById(R.id.listview);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);

        return rootView;
    }

    public void requestNews() {
        newscompany = 1;
        String url = allURL[newscompany][category];
        //String url = "http://www.chosun.com/site/data/rss/rss.xml";
        StringRequest stringRequest = new UTF8StringRequest(Request.Method.GET, url, new
                Response.Listener<String>() {
                    public void onResponse(String response) {
                        mResult = response;
                        parseXMLAndStoreIt();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "서버 에러", Toast.LENGTH_LONG).show();
            }
        });
        stringRequest.setTag(NEWSTAG);
        mQueue.add(stringRequest);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(),"z",Toast.LENGTH_SHORT).show();
       Intent intent = new Intent(getActivity(),NewsViewActivity.class);

        intent.putExtra("title",mArray.get(position).getTitle());
        intent.putExtra("content",mArray.get(position).getContent());
        intent.putExtra("author",mArray.get(position).getAuthor());
        intent.putExtra("link",mArray.get(position).getLink());

        startActivity(intent);
    }

    public class UTF8StringRequest extends StringRequest {
        public UTF8StringRequest(int method, String url, Response.Listener<String> listener,
                                 Response.ErrorListener errorListener) {
            super(method, url, listener, errorListener);
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            String utf8String = null;
            try {
                utf8String = new String(response.data, "UTF-8");
                return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            }
        }
    }

    //--------------------------------------------------------------
    public void parseXMLAndStoreIt() {
        int event;
        String text = null;
        boolean item = false;
        try {
            String rssNews = mResult;
            xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();
            myParser.setInput(new StringReader(rssNews));

            event = myParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {

                String name = myParser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:
                        break;

                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (name.equals("item"))
                            item = true;
                        if (name.equals("title")) {
                            title = text;
                        } else if (name.equals("description")) {
                            content = text;
                        } else if (name.equals("author")) {
                            author = text;
                        } else if (name.equals("link")) {
                            link = text;
                        }
                        break;
                }
                if (title != "" && content != "" && link !="" && item) { //&& author != ""
                    mArray.add(new NewsInfo(title, content, author,link));
                    title = "";
                    content = "";
                    author = "";
                    link = "";
                    item = false;
                }
                event = myParser.next();
            }

            tmpArray = (ArrayList<NewsInfo>)mArray.clone();
            tmpArray2 = (ArrayList<NewsInfo>)tmpArray.clone();
            allArray.addAll(tmpArray2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAdapter.notifyDataSetChanged();
    }

    public class NewsAdapter extends ArrayAdapter<NewsInfo> {
        private LayoutInflater inflater = null;


        public NewsAdapter(Context context, int resource) {
            super(context, resource);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mArray.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NewsViewHolder nvh = null;

            if (convertView == null) {

                convertView = inflater.inflate(R.layout.news_list, parent, false);

                nvh = new NewsViewHolder();
                nvh.txTitle = (TextView) convertView.findViewById(R.id.title);
                //nvh.txContent = (TextView) convertView.findViewById(R.id.content);
                nvh.txAuthor = (TextView) convertView.findViewById(R.id.author);
                //nvh.wvContent = (WebView) convertView.findViewById(R.id.wvContent);

                convertView.setTag(nvh);
            } else {
                nvh = (NewsViewHolder) convertView.getTag();
            }

            NewsInfo info = mArray.get(position);
            if (info != null) {
                nvh.txTitle.setText("제목 : " + info.getTitle());
                //nvh.txContent.setText(info.getContent());
                nvh.txAuthor.setText(info.getAuthor());
                //nvh.wvContent.loadData(info.getContent(), "text/html; charset=UTF-8", null);

            }
            return convertView;
        }

        public Filter getFilter() {
            if (planetFilter == null)
                planetFilter = new PlanetFilter();

            return planetFilter;
        }
    }


    private class PlanetFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
                 FilterResults results = new FilterResults();

                if (constraint == "" || constraint.length() == 0) {
                    results.values = tmpArray;
                    results.count = tmpArray.size();
                } else {


                    ArrayList<NewsInfo>     nNewsList = new ArrayList<NewsInfo>();
                    int i=0;
                    for ( i = 0; i <40; i++) {
                        NewsInfo info = tmpArray.get(i);
                        String string = info.getTitle();
                        String string2 = info.getAuthor();
                        String string3 = info.getContent();
                        if (info.getTitle().toUpperCase().matches(".*" + constraint.toString().toUpperCase() + ".*")) {
                            nNewsList.add(info);
                        } else if (info.getAuthor().toUpperCase().matches(".*" + constraint.toString().toUpperCase() + ".*")) {

                            nNewsList.add(info);
                        } else if (info.getContent().toUpperCase().matches(".*" + constraint.toString().toUpperCase() + ".*")) {

                            nNewsList.add(info);
                        }


                    }


                    results.values = nNewsList;
                    results.count = nNewsList.size();
                }

                return results;

        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count == 0){
                mAdapter.notifyDataSetInvalidated();
                int tmp = tmpArray.size();
                mArray.clear();
                tmp = tmpArray.size();
                tmpArray = (ArrayList<NewsInfo>)tmpArray2.clone();
            }
            else {
                mArray = (ArrayList<NewsInfo>) results.values;
                mAdapter.notifyDataSetChanged();
            }

        }

    }

}