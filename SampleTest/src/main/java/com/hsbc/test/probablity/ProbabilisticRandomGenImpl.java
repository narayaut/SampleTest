package com.hsbc.test.probablity;

import java.util.*;

// implementation of inverse transform sampling
public class ProbabilisticRandomGenImpl implements ProbabilisticRandomGen {

    private final Float[] cumFrequencies;
    private int[] numbers;

    private Random random;
    private float cumFrequency = 0f;

    public ProbabilisticRandomGenImpl(List<NumAndProbability> samples) {
        numbers = new int[samples.size()];
        cumFrequencies = new Float[samples.size()];
        Set<Integer> unique = new HashSet<>();

        random = new Random();
        for(int i=0;i<samples.size();i++) {
            NumAndProbability numAndProbability = samples.get(i);
            if (unique.add(numAndProbability.getNumber())) {
                numbers[i] = numAndProbability.getNumber();
                cumFrequency = numAndProbability.getProbabilityOfSample() + cumFrequency;
                cumFrequencies[i] = cumFrequency;
            }else{
                throw new IllegalArgumentException("Number "+numAndProbability.getNumber()+ " is present more than once.");
            }
        }
    }

    @Override
    public int nextFromSample() {
        float randomKey = this.random.nextFloat() * cumFrequency;
        int closestIndex = BinarySearch.indexOfClosestElement(cumFrequencies, randomKey);
        return numbers[closestIndex];
    }

    public static void main(String[] args) {
        List<NumAndProbability> samples = new ArrayList<>();
        samples.add(new NumAndProbability(1, 0.1f));
        samples.add(new NumAndProbability(2, 0.2f));
        samples.add(new NumAndProbability(3, 0.8f));
        samples.add(new NumAndProbability(4, 1.2f));
        samples.add(new NumAndProbability(5, 4.8f));
        samples.add(new NumAndProbability(6, 0.3f));


        ProbabilisticRandomGenImpl gen = new ProbabilisticRandomGenImpl(samples);
        for(int i=0;i<10;i++) {
            System.out.println(gen.nextFromSample());
        }

    }
}
