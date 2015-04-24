package io.github.hidroh.materialistic;

import android.support.v7.widget.RecyclerView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowPreferenceManager;
import org.robolectric.util.SupportFragmentTestUtil;

import javax.inject.Inject;
import javax.inject.Named;

import io.github.hidroh.materialistic.data.ItemManager;
import io.github.hidroh.materialistic.test.TestInjectableActivity;
import io.github.hidroh.materialistic.test.TestItem;
import io.github.hidroh.materialistic.widget.SinglePageItemRecyclerViewAdapter;
import io.github.hidroh.materialistic.widget.ToggleItemViewHolder;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.reset;

@RunWith(RobolectricTestRunner.class)
public class ItemFragmentSinglePageTest {
    @Inject
    @Named(ActivityModule.HN)
    ItemManager hackerNewsClient;
    @Captor
    ArgumentCaptor<ItemManager.ResponseListener<ItemManager.Item>> listener;
    private RecyclerView recyclerView;
    private SinglePageItemRecyclerViewAdapter adapter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TestApplication.applicationGraph.inject(this);
        reset(hackerNewsClient);
        ShadowPreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                .edit()
                .putString(RuntimeEnvironment.application.getString(R.string.pref_comment_display),
                        RuntimeEnvironment.application.getString(R.string.pref_comment_display_value_single))
                .commit();
        final TestItem item2 = new TestItem() { // level 2
            @Override
            public String getId() {
                return "3";
            }

            @Override
            public int getKidCount() {
                return 0;
            }

            @Override
            public ItemManager.Item[] getKidItems() {
                return new ItemManager.Item[]{};
            }
        };
        final TestItem item1 = new TestItem() { // level 1
            @Override
            public String getId() {
                return "2";
            }

            @Override
            public String getText() {
                return "text";
            }

            @Override
            public int getKidCount() {
                return 1;
            }

            @Override
            public ItemManager.Item[] getKidItems() {
                return new ItemManager.Item[]{item2};
            }
        };
        final TestItem item0 = new TestItem() { // level 0
            @Override
            public String getId() {
                return "1";
            }

            @Override
            public int getLevel() {
                return 1;
            }

            @Override
            public String getText() {
                return "text";
            }

            @Override
            public int getKidCount() {
                return 1;
            }

            @Override
            public ItemManager.Item[] getKidItems() {
                return new ItemManager.Item[]{item1};
            }
        };
        ItemFragment fragment = ItemFragment.instantiate(RuntimeEnvironment.application,
                new TestItem() {
                    @Override
                    public ItemManager.Item[] getKidItems() {
                        return new ItemManager.Item[]{item0};
                    }
                }, null);
        SupportFragmentTestUtil.startVisibleFragment(fragment, TestInjectableActivity.class,
                android.R.id.content);
        recyclerView = (RecyclerView) fragment.getView().findViewById(R.id.recycler_view);
        adapter = (SinglePageItemRecyclerViewAdapter) recyclerView.getAdapter();
    }

    @Test
    public void testExpand() {
        assertEquals(1, adapter.getItemCount());
        ToggleItemViewHolder viewHolder = adapter.createViewHolder(recyclerView, 0);
        adapter.bindViewHolder(viewHolder, 0);
        assertEquals(2, adapter.getItemCount()); // should add kid to adapter
        adapter.bindViewHolder(viewHolder, 0);
        assertEquals(2, adapter.getItemCount()); // should not add again
    }

    @Test
    public void testGetItemType() {
        assertEquals(0, adapter.getItemViewType(0));
    }

    @Test
    public void testPendingItem() {
        ItemFragment fragment = ItemFragment.instantiate(RuntimeEnvironment.application,
                new TestItem() {
                    @Override
                    public ItemManager.Item[] getKidItems() {
                        return new ItemManager.Item[]{new TestItem() {
                            @Override
                            public int getLocalRevision() {
                                return -1;
                            }
                        }};
                    }
                }, null);
        SupportFragmentTestUtil.startVisibleFragment(fragment, TestInjectableActivity.class,
                android.R.id.content);
        recyclerView = (RecyclerView) fragment.getView().findViewById(R.id.recycler_view);
        adapter = (SinglePageItemRecyclerViewAdapter) recyclerView.getAdapter();
        ToggleItemViewHolder viewHolder = adapter.createViewHolder(recyclerView, 0);
        adapter.bindViewHolder(viewHolder, 0);
    }

    @Test
    public void testToggle() {
        // expand all
        ToggleItemViewHolder viewHolder = adapter.createViewHolder(recyclerView, 0);
        adapter.bindViewHolder(viewHolder, 0);
        ToggleItemViewHolder viewHolder1 = adapter.createViewHolder(recyclerView, 1);
        adapter.bindViewHolder(viewHolder1, 1);
        ToggleItemViewHolder viewHolder2 = adapter.createViewHolder(recyclerView, 2);
        adapter.bindViewHolder(viewHolder2, 2);
        assertEquals(3, adapter.getItemCount());

        // collapse all
        viewHolder.itemView.findViewById(R.id.toggle).performClick();
        adapter.bindViewHolder(viewHolder, 0);
        assertEquals(1, adapter.getItemCount());

        // expand again, should add item when binding
        viewHolder.itemView.findViewById(R.id.toggle).performClick();
        adapter.bindViewHolder(viewHolder, 0);
        adapter.bindViewHolder(viewHolder1, 1);
        adapter.bindViewHolder(viewHolder2, 2);
        assertEquals(3, adapter.getItemCount());
    }

    @After
    public void tearDown() {
        reset(hackerNewsClient);
    }
}
