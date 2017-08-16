# Pacifier

My take on the concept covered by Dart & Henson. By annotating your Activity extras/Fragment arguments, it generates all necessary boilerplate to create new instances of the Activity/Fragment and to populate the arguments. Pacifier gets rid of arguments, get it?

Use the `@Extra` annotation in an activity as follows:

```
public class MyActivity extends AppCompatActivity {

  @Extra
  String myStringExtra = "defaultValue";
  
  @Extra
  int myIntegerExtra;
  
  @Override
  protected void onCreate(Bundle savedInstanceState {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.my_activity);
    MyActivityExtras.bind(this);
  }

}
```

This will generate the helper class MyActivityExtras, which contains the bind method seen above that will populate the annotated extras with their values. Note that default values can be provided by simply assigning them to the member - if the argument is not provided, the default value will remain. The helper class also contains the following factory method that must be used to launch MyActivity:

```
public static Intent newIntent(Context context, String myStringExtra, int myIntegerExtra) {
  Intent intent = new Intent(context, MyActivity.class);
  intent.putExtra("myStringExtra", myStringExtra);
  intent.putExtra("myIntegerExtra", myIntegerExtra);
  return intent;
}
```

The same thing can be done with the `@Argument` annotation in Fragments. Note that this will produce both an "args" and a "newInstance" method, in case you need just the bundle.

Now, Dart & Henson generates builders, while Pacifier creates factory methods. I chose this route because it enforces a contract for instantiating an Activity/Fragment, preventing potential misuse that can lead to unintended behavior/crashes when not enough or too many arguments are supplied. However, this means we have to account for different entry paths into the Activity/Fragment with potentially differing arguments. To deal with this, we add an argument to our `@Extra` or `@Argument` annotations called "path". See below:

```
public class SampleFragment extends Fragment {

    private static final String SECOND_PATH = "SecondPath";
    private static final String THIRD_PATH = "ThirdPath";
    private static final String FOURTH_PATH = "FourthPath";

    @Argument
    String stringArgument;

    @Argument
    double doubleArgument;

    @Argument(path=SECOND_PATH)
    int integerArgument;

    @Argument(paths={SECOND_PATH, THIRD_PATH})
    int anotherIntArgument;

    @Argument(path=THIRD_PATH)
    String anotherStringArgument = "defaultValue";

    @Argument(path=FOURTH_PATH)
    SomeObject objectArgument;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SampleFragmentArguments.bind(this);
    }

}
```

If an argument is necessary to the Fragment no matter what, don't bother giving it an argument - it will be included in the parameters for all the factory methods. If an argument will only be in a single path, use `path="PathName"` as an argument. If it is in multiple paths, but not all of them, then use `paths={"PathName1", "PathName2"}`. The class above will generate the following class:

```
public class SampleFragmentArguments {
  public static Bundle args(String stringArgument, double doubleArgument) {
    Bundle args = new Bundle();
    args.putString("stringArgument", stringArgument);
    args.putDouble("doubleArgument", doubleArgument);
    return args;
  }

  public static Bundle argsThirdPath(String stringArgument, double doubleArgument,
      int anotherIntArgument, String anotherStringArgument) {
    Bundle args = new Bundle();
    args.putString("stringArgument", stringArgument);
    args.putDouble("doubleArgument", doubleArgument);
    args.putInt("anotherIntArgument", anotherIntArgument);
    args.putString("anotherStringArgument", anotherStringArgument);
    return args;
  }

  public static Bundle argsSecondPath(String stringArgument, double doubleArgument,
      int integerArgument, int anotherIntArgument) {
    Bundle args = new Bundle();
    args.putString("stringArgument", stringArgument);
    args.putDouble("doubleArgument", doubleArgument);
    args.putInt("integerArgument", integerArgument);
    args.putInt("anotherIntArgument", anotherIntArgument);
    return args;
  }

  public static Bundle argsFourthPath(String stringArgument, double doubleArgument,
      SomeObject objectArgument) {
    Bundle args = new Bundle();
    args.putString("stringArgument", stringArgument);
    args.putDouble("doubleArgument", doubleArgument);
    args.putSerializable("objectArgument", objectArgument);
    return args;
  }

  public static SampleFragment newInstance(String stringArgument, double doubleArgument) {
    SampleFragment fragment = new SampleFragment();
    fragment.setArguments(args(stringArgument, doubleArgument));
    return fragment;
  }

  public static SampleFragment newInstanceThirdPath(String stringArgument, double doubleArgument,
      int anotherIntArgument, String anotherStringArgument) {
    SampleFragment fragment = new SampleFragment();
    fragment.setArguments(argsThirdPath(stringArgument, doubleArgument, anotherIntArgument, anotherStringArgument));
    return fragment;
  }

  public static SampleFragment newInstanceSecondPath(String stringArgument, double doubleArgument,
      int integerArgument, int anotherIntArgument) {
    SampleFragment fragment = new SampleFragment();
    fragment.setArguments(argsSecondPath(stringArgument, doubleArgument, integerArgument, anotherIntArgument));
    return fragment;
  }

  public static SampleFragment newInstanceFourthPath(String stringArgument, double doubleArgument,
      SomeObject objectArgument) {
    SampleFragment fragment = new SampleFragment();
    fragment.setArguments(argsFourthPath(stringArgument, doubleArgument, objectArgument));
    return fragment;
  }

  static void bind(SampleFragment fragment) {
    Bundle args = fragment.getArguments();
    if (args.containsKey("stringArgument")) fragment.stringArgument = args.getString("stringArgument");
    if (args.containsKey("integerArgument")) fragment.integerArgument = args.getInt("integerArgument");
    if (args.containsKey("anotherIntArgument")) fragment.anotherIntArgument = args.getInt("anotherIntArgument");
    if (args.containsKey("objectArgument")) fragment.objectArgument = (personal.rowan.pacifier.object.SomeObject) args.getSerializable("objectArgument");
    if (args.containsKey("doubleArgument")) fragment.doubleArgument = args.getDouble("doubleArgument");
    if (args.containsKey("anotherStringArgument")) fragment.anotherStringArgument = args.getString("anotherStringArgument");
  }
}
```

Unfortunately, this is not yet available for consumption as a library via Gradle, but please feel free to copy it as needed and provide feedback. Thanks!
