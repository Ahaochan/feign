/**
 * Copyright 2012-2020 The Feign Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package feign.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import java.lang.reflect.Method;
import feign.Feign;
import feign.Target;

/**
 * Used to control properties of a hystrix command. Use cases include reading from static
 * configuration or custom annotations.
 *
 * <p>
 * This is parsed up-front, like {@link feign.Contract}, so will not be invoked for each command
 * invocation.
 *
 * <p>
 * Note: when deciding the
 * {@link com.netflix.hystrix.HystrixCommand.Setter#andCommandKey(HystrixCommandKey) command key},
 * recall it lives in a shared cache, so make sure it is unique.
 */
public interface SetterFactory {

  /**
   * Returns a hystrix setter appropriate for the given target and method
   */
  HystrixCommand.Setter create(Target<?> target, Method method);

  /**
   * Default behavior is to derive the group key from {@link Target#name()} and the command key from
   * {@link Feign#configKey(Class, Method)}.
   */
  final class Default implements SetterFactory {

    @Override
    public HystrixCommand.Setter create(Target<?> target, Method method) {
      // HardCodedTarget的name是@FeignClient标记的服务名
      String groupKey = target.name();
      // HardCodedTarget的type是@FeignClient修饰的接口的Class对象
      // 这里拼接生成方法的唯一标志
      String commandKey = Feign.configKey(target.type(), method);
      // 最简单的Setter就是服务名作为groupKey, 接口拼接成的唯一标志作为commandKey
      // 也就是一个服务一个线程池
      return HystrixCommand.Setter
          .withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
          .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey));
    }
  }
}
