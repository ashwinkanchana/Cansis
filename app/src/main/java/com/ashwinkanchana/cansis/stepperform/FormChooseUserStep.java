package com.ashwinkanchana.cansis.stepperform;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ashwinkanchana.cansis.R;
import com.google.android.material.chip.ChipGroup;

import ernestoyaquello.com.verticalstepperform.Step;

import static com.ashwinkanchana.cansis.utils.Constants.USER_PARENT;
import static com.ashwinkanchana.cansis.utils.Constants.USER_STUDENT;

public class FormChooseUserStep extends Step {
    public static final String TAG = FormChooseUserStep.class.getSimpleName();
    private int USER_TYPE;
    private TextView userTextView;
    private String studentFullName,parentFullName;
    private String infoText;

    public FormChooseUserStep(String title,String studentFullName,String parentFullName) {
        super(title);
        this.studentFullName = studentFullName;
        this.parentFullName = parentFullName;
    }



    @Override
    public Object getStepData() {
        return USER_TYPE;
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        return null;
    }

    @Override
    public void restoreStepData(Object data) {

    }

    @Override
    protected IsDataValid isStepDataValid(Object stepData) {
        if ( stepData.equals(USER_PARENT)  || stepData.equals(USER_STUDENT) )
            return new IsDataValid(true);
        else return new IsDataValid(false);
    }

    @Override
    protected View createStepContentLayout() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View selectUserStep = inflater.inflate(R.layout.step_login, null, false);
        ChipGroup chipGroup =  selectUserStep.findViewById(R.id.userChipGroup);
        userTextView = selectUserStep.findViewById(R.id.user_name_text);
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                infoText = "Logging in as ";
                String info;
                if(checkedId == R.id.studentLogin){
                    USER_TYPE = USER_STUDENT;
                    info = infoText+studentFullName;
                    userTextView.setText(info);
                    markAsCompleted(true);

                }else if(checkedId == R.id.parentLogin){
                    USER_TYPE = USER_PARENT;
                    info = infoText+parentFullName;
                    userTextView.setText(info);
                    markAsCompleted(true);
                }
            }
        });
        chipGroup.check(R.id.studentButton);
        return selectUserStep;
    }

    @Override
    protected void onStepOpened(boolean animated) {

    }

    @Override
    protected void onStepClosed(boolean animated) {

    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {

    }

    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {

    }

}
